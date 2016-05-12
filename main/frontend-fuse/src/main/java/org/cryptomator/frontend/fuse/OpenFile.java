package org.cryptomator.frontend.fuse;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.cryptomator.filesystem.File;
import org.cryptomator.filesystem.FileSystem;
import org.cryptomator.filesystem.ReadableFile;
import org.cryptomator.filesystem.WritableFile;

import net.fusejna.StructFuseFileInfo.FileInfoWrapper.OpenMode;

public class OpenFile {

	private static final AtomicLong NEXT_HANDLE = new AtomicLong(1L);
	
	private final Long handle;
	private final OpenMode mode;
	private final File file;
	private final Consumer<OpenFile> afterClose;
	
	private final ReadableFile readable;
	private final WritableFile writable;
	
	private OpenFile(String path, FileSystem fileSystem, OpenMode mode, Consumer<OpenFile> afterClose) throws OpenException {
		this.handle = NEXT_HANDLE.getAndIncrement();
		this.mode = mode;
		this.file = fileSystem.resolveFile(path);
		this.afterClose = afterClose;
		if (canRead()) {
			readable = file.openReadable();
		} else {
			readable = null;
		}
		if (canWrite()) {
			writable = file.openWritable();
		} else {
			writable = null;
		}
	}

	private boolean canWrite() {
		return mode != OpenMode.READONLY;
	}

	private boolean canRead() {
		return mode != OpenMode.WRITEONLY;
	}

	public void write(long position, ByteBuffer buffer) {
		if (!canWrite()) {
			throw new UncheckedIOException(new IOException("File not open for writing"));
		}
		writable.position(position);
		writable.write(buffer);
	}

	public void read(long position, ByteBuffer buffer) {
		if (!canRead()) {
			throw new UncheckedIOException(new IOException("File not open for reading"));
		}
		readable.position(position);
		readable.read(buffer);
	}

	public void close() {
		afterClose.accept(this);
		if (writable != null) {
			writable.close();
		}
		if (readable != null) {
			readable.close();
		}
	}

	public Long handle() {
		return handle;
	}
	
	public static Builder open(String path) {
		return new Builder(path);
	}

	public boolean openWithModeIsAcceptable(OpenMode openMode) {
		return openMode == OpenMode.READONLY //
				&& mode == OpenMode.READONLY;
	}
	
	public static class Builder {
		
		private String path;
		private FileSystem fileSystem;
		private Consumer<OpenFile> afterClose;
		
		private Builder(String path) {
			this.path = path;
		}
		
		public Builder from(FileSystem fileSystem) {
			this.fileSystem = fileSystem;
			return this;
		}
		
		public Builder afterClose(Consumer<OpenFile> afterClose) {
			this.afterClose = afterClose;
			return this;
		}
		
		public OpenFile inMode(OpenMode mode) throws OpenException {
			validate();
			return new OpenFile(path, fileSystem, mode, afterClose);
		}
		
		private void validate() {
			if (isEmpty(path)) {
				throw new IllegalStateException();
			}
		}
		
	}
	
}
