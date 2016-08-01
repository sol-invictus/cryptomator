package org.cryptomator.frontend.fuse.impl;

import static org.cryptomator.frontend.fuse.api.StandardFuseResult.CAN_NOT_GROW_FILE_THROUGH_TRUNCATE;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.SUCCESS;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;

import org.cryptomator.common.RunnableThrowingException;
import org.cryptomator.common.SupplierThrowingException;
import org.cryptomator.frontend.fuse.api.FuseResult;

class OpenFile {

	private final Path path;
	private final AsynchronousFileChannel channel;
	
	public OpenFile(Path path, NioAccess nioAccess) {
		this.path = path;
		this.channel = wrapIoExceptions(() -> nioAccess.openAsyncFileChannel(path, StandardOpenOption.READ, StandardOpenOption.WRITE));
	}

	public boolean hasPath(Path path) {
		return this.path.equals(path);
	}

	public Path path() {
		return path;
	}

	public FuseResult read(ByteBuffer buffer, long size, long offset) {
		return wrapIoExceptions(() -> {
			int bytesRead;
			try {
				bytesRead = channel.read(buffer, offset).get();
			} catch (ExecutionException | InterruptedException e) {
				throw new IOException(e);
			}
			if (bytesRead == -1) {
				bytesRead = 0;
			}
			return FuseResult.withValue(bytesRead);
		});
	}

	public FuseResult write(ByteBuffer buffer, long size, long offset) {
		return wrapIoExceptions(() -> {
			int bytesWritten;
			try {
				bytesWritten = channel.write(buffer, offset).get();
			} catch (ExecutionException | InterruptedException e) {
				throw new IOException(e);
			}
			return FuseResult.withValue(bytesWritten);
		});
	}

	public FuseResult flush() {
		return fsync(false);
	}

	public FuseResult fsync(boolean flushMetadata) {
		return wrapIoExceptions(() -> {
			channel.force(flushMetadata);
			return SUCCESS;
		});
	}

	public void release() {
		wrapIoExceptions(() -> {
			try {
				channel.force(true);
			} finally {
				channel.close();
			}
		});
	}

	public FuseResult truncate(long offset) {
		return wrapIoExceptions(() -> {
			if (channel.size() < offset) {
				channel.write(ByteBuffer.allocate(1), offset);
			}
			channel.truncate(offset);
			return SUCCESS;
		});
	}
	
	private <T> T wrapIoExceptions(SupplierThrowingException<T, IOException> supplier) {
		try {
			return supplier.get();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private void wrapIoExceptions(RunnableThrowingException<IOException> task) {
		try {
			task.run();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
