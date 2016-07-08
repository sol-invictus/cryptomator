package org.cryptomator.frontend.fuse.impl;

import static org.cryptomator.frontend.fuse.api.StandardFuseResult.FILE_DOES_NOT_EXIST;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.FILE_EXISTS;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.INVALID_FILE_HANDLE;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.SUCCESS;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.UNSUPPORTED_OPERATION;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

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
	public FuseResult create(String path) {
		Path resolved = resolve(path);
		if (nioAccess.exists(resolved)) {
			return FILE_EXISTS;
		} else {
			return wrapIoExceptions(() -> {
				nioAccess.createFile(resolved);
				return SUCCESS;
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
		return openFile(path, fileHandle).apply(OpenFile::flush);
	}

	@Override
	public FuseResult fsync(String path, boolean flushMetadata, FileHandle fileHandle) {
		return openFile(path, fileHandle).apply(openFile -> openFile.fsync(flushMetadata));
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
		return openFiles.release(resolve(path), fileHandle);
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
		return StandardFuseResult.IO_ERROR; // TODO
	}

	@Override
	public FuseResult rmdir(String path) {
		return StandardFuseResult.IO_ERROR; // TODO
	}

	@Override
	public FuseResult statfs(String path, FilesystemStats stats) {
		return StandardFuseResult.IO_ERROR; // TODO
	}

	@Override
	public FuseResult truncate(String path, long offset) {
		return StandardFuseResult.IO_ERROR; // TODO
	}

	@Override
	public FuseResult unlink(String path) {
		return StandardFuseResult.IO_ERROR; // TODO
	}

	@Override
	public FuseResult utimens(String path, Times times) {
		return StandardFuseResult.IO_ERROR; // TODO
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
	
	@FunctionalInterface
	private interface SupplierThrowingException<ResultType,ErrorType extends Throwable> {
		
		ResultType get() throws ErrorType;
		
	}

}
