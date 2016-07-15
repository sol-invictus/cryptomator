package org.cryptomator.frontend.fuse.impl;

import static org.cryptomator.frontend.fuse.api.StandardFuseResult.FILE_DOES_NOT_EXIST;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.IO_ERROR;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.NoSuchFileException;
import java.util.function.Consumer;

import org.cryptomator.frontend.fuse.api.Attributes;
import org.cryptomator.frontend.fuse.api.FileHandle;
import org.cryptomator.frontend.fuse.api.FilesystemStats;
import org.cryptomator.frontend.fuse.api.FuseOperations;
import org.cryptomator.frontend.fuse.api.FuseResult;
import org.cryptomator.frontend.fuse.api.Times;
import org.cryptomator.frontend.fuse.api.WritableFileHandle;
import org.slf4j.Logger;

public class ExceptionHandlingFuseOperationsDecorator implements FuseOperations {

	private final Logger fuseLogger;
	private final FuseOperations delegate;

	public ExceptionHandlingFuseOperationsDecorator(FuseOperations delegate, Logger fuseLogger) {
		this.delegate = delegate;
		this.fuseLogger = fuseLogger;
	}

	@Override
	public FuseResult access(String path) {
		try {
			return delegate.access(path);
		} catch (Throwable e) {
			log("access", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult create(String path) {
		try {
			return delegate.create(path);
		} catch (Throwable e) {
			log("create", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult fgetattr(String path, Attributes attributes, FileHandle fileHandle) {
		try {
			return delegate.fgetattr(path, attributes, fileHandle);
		} catch (Throwable e) {
			log("fgetattr", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult flush(String path, FileHandle fileHandle) {
		try {
			return delegate.flush(path, fileHandle);
		} catch (Throwable e) {
			log("flush", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult fsync(String path, boolean flushMetadata, FileHandle fileHandle) {
		try {
			return delegate.fsync(path, flushMetadata, fileHandle);
		} catch (Throwable e) {
			log("fsync", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult fsyncdir(String path, boolean flushMetadata, FileHandle fileHandle) {
		try {
			return delegate.fsyncdir(path, flushMetadata, fileHandle);
		} catch (Throwable e) {
			log("fsyncdir", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult ftruncate(String path, long offset, FileHandle fileHandle) {
		try {
			return delegate.ftruncate(path, offset, fileHandle);
		} catch (Throwable e) {
			log("ftruncate", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult getattr(String path, Attributes attributes) {
		try {
			return delegate.getattr(path, attributes);
		} catch (Throwable e) {
			log("getattr", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult lock(String path) {
		try {
			return delegate.lock(path);
		} catch (Throwable e) {
			log("lock", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult mkdir(String path) {
		try {
			return delegate.mkdir(path);
		} catch (Throwable e) {
			log("mkdir", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult open(String path, WritableFileHandle fileHandleConsumer) {
		try {
			return delegate.open(path, fileHandleConsumer);
		} catch (Throwable e) {
			log("open", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult opendir(String path, WritableFileHandle fileHandleConsumer) {
		try {
			return delegate.opendir(path, fileHandleConsumer);
		} catch (Throwable e) {
			log("opendir", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult read(String path, ByteBuffer buffer, long size, long offset, FileHandle fileHandle) {
		try {
			return delegate.read(path, buffer, size, offset, fileHandle);
		} catch (Throwable e) {
			log("read", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult readdir(String path, Consumer<String> filler) {
		try {
			return delegate.readdir(path, filler);
		} catch (Throwable e) {
			log("readdir", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult release(String path, FileHandle fileHandle) {
		try {
			return delegate.release(path, fileHandle);
		} catch (Throwable e) {
			log("release", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult releasedir(String path, FileHandle fileHandle) {
		try {
			return delegate.releasedir(path, fileHandle);
		} catch (Throwable e) {
			log("releasedir", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult rename(String path, String newPath) {
		try {
			return delegate.rename(path, newPath);
		} catch (Throwable e) {
			log("rename", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult rmdir(String path) {
		try {
			return delegate.rmdir(path);
		} catch (Throwable e) {
			log("rmdir", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult statfs(String path, FilesystemStats stats) {
		try {
			return delegate.statfs(path, stats);
		} catch (Throwable e) {
			log("statfs", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult truncate(String path, long offset) {
		try {
			return delegate.truncate(path, offset);
		} catch (Throwable e) {
			log("truncate", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult unlink(String path) {
		try {
			return delegate.unlink(path);
		} catch (Throwable e) {
			log("unlink", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult utimens(String path, Times times) {
		try {
			return delegate.utimens(path, times);
		} catch (Throwable e) {
			log("utimens", e);
			return errorCodeFor(e);
		}
	}

	@Override
	public FuseResult write(String path, ByteBuffer buffer, long bufSize, long writeOffset, FileHandle fileHandle) {
		try {
			return delegate.write(path, buffer, bufSize, writeOffset, fileHandle);
		} catch (Throwable e) {
			log("write", e);
			return errorCodeFor(e);
		}
	}

	private void log(String operation, Throwable e) {
		if (containsCause(e, UncheckedIOException.class)) {
			if (fuseLogger.isTraceEnabled()) {
				fuseLogger.trace("Error during " + operation + " operation", e);
			}
		} else if (fuseLogger.isInfoEnabled()) {
			fuseLogger.info("Error during " + operation + " operation", e);
		} else {
			fuseLogger.error("Error during " + operation + " operation: " + e.getMessage());
		}
	}
	
	private FuseResult errorCodeFor(Throwable e) {
		if (containsCause(e, FileNotFoundException.class) || containsCause(e, NoSuchFileException.class)) {
			return FILE_DOES_NOT_EXIST;
		} else {
			return IO_ERROR;
		}
	}

	private boolean containsCause(Throwable e, Class<? extends Throwable> type) {
		if (e == null) {
			return false;
		}
		if (type.isInstance(e)) {
			return true;
		}
		return containsCause(e.getCause(), type);
	}

}
