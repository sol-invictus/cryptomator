package org.cryptomator.frontend.fuse.impl;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.DIRECTORY_NOT_EMPTY;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.FILE_DOES_NOT_EXIST;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.FILE_EXISTS;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.INVALID_FILE_HANDLE;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.IS_DIRECTORY;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.IS_NO_DIRECTORY;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.SUCCESS;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.UNSUPPORTED_OPERATION;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.cryptomator.common.SupplierThrowingException;
import org.cryptomator.frontend.fuse.api.Attributes;
import org.cryptomator.frontend.fuse.api.FileHandle;
import org.cryptomator.frontend.fuse.api.FilesystemStats;
import org.cryptomator.frontend.fuse.api.FuseOperations;
import org.cryptomator.frontend.fuse.api.FuseResult;
import org.cryptomator.frontend.fuse.api.StandardFuseResult;
import org.cryptomator.frontend.fuse.api.Times;
import org.cryptomator.frontend.fuse.api.WritableFileHandle;

class NioFuseOperations implements FuseOperations {
	
	private final Path root;
	private final OpenFiles openFiles;
	private final NioAccess nioAccess;
	
	private final GetattrCache getattrCache;
	
	public NioFuseOperations(Path root, NioAccess nioAccess, OpenFiles openFiles, GetattrCacheFactory getattrCacheFactory) {
		this.root = root;
		this.openFiles = openFiles;
		this.nioAccess = nioAccess;
		this.getattrCache = getattrCacheFactory.create(this::getAttrImpl);
	}
	
	@Override
	public FuseResult access(String path) {
		if (nioAccess.exists(resolve(path))) {
			return SUCCESS;
		} else {
			return FILE_DOES_NOT_EXIST;
		}
	}

	@Override
	public FuseResult create(String path, WritableFileHandle fileHandleConsumer) {
		Path resolved = resolve(path);
		if (nioAccess.exists(resolved)) {
			return FILE_EXISTS;
		} else {
			return wrapIoExceptions(() -> {
				nioAccess.createFile(resolved);
				getattrCache.evict(resolved);
				return openFiles.open(resolved, fileHandleConsumer);
			});
		}
	}

	@Override
	public FuseResult fgetattr(String path, Attributes attributes, FileHandle fileHandle) {
		return openFile(path, fileHandle).apply(openFile -> {
			getattrCache.reload(openFile.path()).fill(attributes);
			return SUCCESS;
		});
	}

	@Override
	public FuseResult flush(String path, FileHandle fileHandle) {
		try {
			return openFile(path, fileHandle).apply(OpenFile::flush);
		} finally {
			getattrCache.evict(resolve(path));
		}
	}

	@Override
	public FuseResult fsync(String path, boolean flushMetadata, FileHandle fileHandle) {
		try {
			return openFile(path, fileHandle).apply(openFile -> openFile.fsync(flushMetadata));
		} finally {
			getattrCache.evict(resolve(path));
		}
	}

	@Override
	public FuseResult fsyncdir(String path, boolean flushMetadata, FileHandle fileHandle) {
		if (nioAccess.exists(resolve(path))) {
			return SUCCESS;
		} else {
			return FILE_DOES_NOT_EXIST;
		}
	}

	@Override
	public FuseResult ftruncate(String path, long offset, FileHandle fileHandle) {
		return openFile(path, fileHandle).apply(openFile -> openFile.truncate(offset));
	}

	@Override
	public FuseResult getattr(String path, Attributes attributes) {
		getattrCache.get(resolve(path)).fill(attributes);
		return SUCCESS;
	}

	@Override
	public FuseResult lock(String path) {
		return UNSUPPORTED_OPERATION;
	}

	@Override
	public FuseResult mkdir(String path) {
		Path resolved = resolve(path);
		if (nioAccess.exists(resolved)) {
			return FILE_EXISTS;
		} else {
			return wrapIoExceptions(() -> {
				nioAccess.createDirectory(resolved);
				getattrCache.evict(resolved);
				return SUCCESS;
			});
		}
	}

	@Override
	public FuseResult open(String path, WritableFileHandle fileHandleConsumer) {
		return openFiles.open(resolve(path), fileHandleConsumer);
	}

	@Override
	public FuseResult opendir(String path, WritableFileHandle fileHandleConsumer) {
		if (nioAccess.exists(resolve(path))) {
			return SUCCESS;
		} else {
			return FILE_DOES_NOT_EXIST;
		}
	}

	@Override
	public FuseResult read(String path, ByteBuffer buffer, long size, long offset, FileHandle fileHandle) {
		return openFile(path, fileHandle).apply(openFile -> openFile.read(buffer, size, offset));
	}

	@Override
	public FuseResult readdir(String path, Consumer<String> filler) {
		Path resolved = resolve(path);
		if (!nioAccess.exists(resolved)) {
			return FILE_DOES_NOT_EXIST;
		}
		return wrapIoExceptions(() -> {
			nioAccess.list(resolved) //
				.map(Path::getFileName) //
				.map(Path::toString) //
				.forEach(filler);
			return SUCCESS;
		});
	}

	@Override
	public FuseResult release(String path, FileHandle fileHandle) {
		try {
			return openFiles.release(resolve(path), fileHandle);
		} finally {
			getattrCache.evict(resolve(path));
		}
	}

	@Override
	public FuseResult releasedir(String path, FileHandle fileHandle) {
		if (nioAccess.exists(resolve(path))) {
			return SUCCESS;
		} else {
			return FILE_DOES_NOT_EXIST;
		}
	}

	@Override
	public FuseResult rename(String path, String newPath) {
		Path from = resolve(path);
		Path to = resolve(newPath);
		return wrapIoExceptions(()-> {
			if (to.startsWith(from)) {
				return StandardFuseResult.ILLEGAL_ARGUMENTS;
			}
			if (!nioAccess.exists(from) || !nioAccess.exists(to.getParent())) {
				return StandardFuseResult.FILE_DOES_NOT_EXIST;
			}
			if (nioAccess.isDirectory(to)) {
				if (!nioAccess.isDirectory(from)) {
					return StandardFuseResult.IS_NO_DIRECTORY;
				}
				if (nioAccess.list(to).count() > 0) {
					return StandardFuseResult.DIRECTORY_NOT_EMPTY;
				}
			} else if (nioAccess.isRegularFile(to)) {
				if (!nioAccess.isRegularFile(from)) {
					return StandardFuseResult.IS_DIRECTORY;
				}
			}
			nioAccess.move(from, to, REPLACE_EXISTING);
			getattrCache.evict(from);
			getattrCache.evict(to);
			return StandardFuseResult.SUCCESS;
		});
	}

	@Override
	public FuseResult rmdir(String path) {
		Path resolved = resolve(path);
		if (nioAccess.isDirectory(resolved)) {
			return wrapIoExceptions(() -> {
				if (nioAccess.list(resolved).count() > 0) {
					return DIRECTORY_NOT_EMPTY;
				}
				nioAccess.delete(resolved);
				getattrCache.evict(resolved);
				return SUCCESS;
			});
		} else if (nioAccess.exists(resolved)) {
			return IS_NO_DIRECTORY;
		} else {
			return FILE_DOES_NOT_EXIST;
		}
	}

	@Override
	public FuseResult statfs(String path, FilesystemStats stats) {
		return wrapIoExceptions(() -> {
			FileStore fileStore = nioAccess.getFileStore(root);
			long total = fileStore.getTotalSpace();
			long free = fileStore.getUnallocatedSpace();
			stats.available(free);
			stats.used(total - free);
			return SUCCESS;
		});
	}

	@Override
	public FuseResult truncate(String path, long offset) {
		Path resolved = resolve(path);
		if (!nioAccess.exists(resolved)) {
			return FILE_DOES_NOT_EXIST;
		}
		OpenFile openFile = openFiles.open(resolved);
		FuseResult result;
		try {
			result = openFile.truncate(offset);
		} finally {
			openFile.release();
		}
		return result;
	}

	@Override
	public FuseResult unlink(String path) {
		Path resolved = resolve(path);
		if (nioAccess.isRegularFile(resolved)) {
			return wrapIoExceptions(() -> {
				nioAccess.delete(resolved);
				return SUCCESS;
			});
		} else {
			if (nioAccess.exists(resolved)) {
				return IS_DIRECTORY;
			} else {
				return FILE_DOES_NOT_EXIST; 
			}
		}
	}

	@Override
	public FuseResult utimens(String path, Times times) {
		Path resolved = resolve(path);
		if (!nioAccess.exists(resolved)) {
			return FILE_DOES_NOT_EXIST;
		}
		return wrapIoExceptions(() -> {
			BasicFileAttributes attributes = nioAccess.readAttributes(resolved, BasicFileAttributes.class);
			times.accessTime(attributes.lastAccessTime().toMillis());
			times.modificationTime(attributes.lastModifiedTime().toMillis());
			return SUCCESS;
		});
	}

	@Override
	public FuseResult write(String path, ByteBuffer buffer, long bufSize, long writeOffset, FileHandle fileHandle) {
		return openFile(path, fileHandle).apply(openFile -> openFile.write(buffer, bufSize, writeOffset));
	}

	private Path resolve(String path) {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		if (path.isEmpty()) {
			return root;
		}
		return root.resolve(path);
	}
	
	private Function<Function<OpenFile,FuseResult>,FuseResult> openFile(String path, FileHandle fileHandle) {
		return operation -> {
			Path resolved = resolve(path);
			Optional<OpenFile> openFile = openFiles.get(resolved, fileHandle);
			if (openFile.isPresent()) {
				return operation.apply(openFile.get());
			} else {
				return INVALID_FILE_HANDLE;
			}
		};
	}
	
	private GetattrResult getAttrImpl(Path path) {
		return wrapIoExceptions(() -> {
			return new GetattrResult(nioAccess.readAttributes(path, BasicFileAttributes.class));
		});
	}
	
	private <T> T wrapIoExceptions(SupplierThrowingException<T,IOException> supplier) {
		try {
			return supplier.get();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
