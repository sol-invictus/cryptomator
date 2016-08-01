package org.cryptomator.frontend.fuse.impl;

import static java.lang.Math.min;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.util.Arrays.copyOf;

import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.cryptomator.frontend.fuse.api.Attributes;
import org.cryptomator.frontend.fuse.api.FileHandle;
import org.cryptomator.frontend.fuse.api.FilesystemStats;
import org.cryptomator.frontend.fuse.api.FuseOperations;
import org.cryptomator.frontend.fuse.api.FuseResult;
import org.cryptomator.frontend.fuse.api.ReadWriteFileHandle;
import org.cryptomator.frontend.fuse.api.StandardFuseResult;
import org.cryptomator.frontend.fuse.api.Times;
import org.cryptomator.frontend.fuse.api.WritableFileHandle;
import org.slf4j.Logger;

public class LoggingFuseOperationsDecorator implements FuseOperations {

	private final Logger fuseLogger;
	private final FuseOperations delegate;
	private volatile boolean logData;
	private volatile boolean logSuccess = true;

	public LoggingFuseOperationsDecorator(FuseOperations delegate, Logger fuseLogger) {
		this.delegate = delegate;
		this.fuseLogger = fuseLogger;
	}
	
	public void setLogSuccess(boolean logData) {
		this.logData = logData;
	}
	
	public void setLogData(boolean logData) {
		this.logData = logData;
	}

	@Override
	public FuseResult access(String path) {
		return log("access(%s)", () -> delegate.access(path), path);
	}

	@Override
	public FuseResult create(String path, WritableFileHandle originalFileHandleConsumer) {
		return logWithHandle("create(%s)", fileHandleConsumer -> delegate.create(path, fileHandleConsumer), originalFileHandleConsumer, path);
	}

	@Override
	public FuseResult fgetattr(String path, Attributes attributes, FileHandle fileHandle) {
		return log("fgetattr(%s,*,%d)", () -> delegate.fgetattr(path, attributes, fileHandle), path, fileHandle.getAsLong());
	}

	@Override
	public FuseResult flush(String path, FileHandle fileHandle) {
		return log("flush(%s,%d)", () -> delegate.flush(path, fileHandle), path, fileHandle.getAsLong());
	}

	@Override
	public FuseResult fsync(String path, boolean flushMetadata, FileHandle fileHandle) {
		return log("fsync(%s,%d)", () -> delegate.fsync(path, flushMetadata, fileHandle), path, fileHandle.getAsLong());
	}

	@Override
	public FuseResult fsyncdir(String path, boolean flushMetadata, FileHandle fileHandle) {
		return log("fsyncdir(%s,%d)", () -> delegate.fsyncdir(path, flushMetadata, fileHandle), path, fileHandle.getAsLong());
	}

	@Override
	public FuseResult ftruncate(String path, long offset, FileHandle fileHandle) {
		return log("ftruncate(%s,%d,%d)", () -> delegate.ftruncate(path, offset, fileHandle), path, offset, fileHandle.getAsLong());
	}

	@Override
	public FuseResult getattr(String path, Attributes attributes) {
		// return log("getattr(%s,*)", () -> delegate.getattr(path, attributes), path);
		return delegate.getattr(path, attributes);
	}

	@Override
	public FuseResult lock(String path) {
		return log("lock(%s)", () -> delegate.lock(path), path);
	}

	@Override
	public FuseResult mkdir(String path) {
		return log("mkdir(%s)", () -> delegate.mkdir(path), path);
	}

	@Override
	public FuseResult open(String path, WritableFileHandle originalFileHandleConsumer) {
		return logWithHandle("open(%s)", fileHandleConsumer -> delegate.open(path, fileHandleConsumer), originalFileHandleConsumer, path);
	}

	@Override
	public FuseResult opendir(String path, WritableFileHandle originalFileHandleConsumer) {
		return logWithHandle("opendir(%s)", fileHandleConsumer -> delegate.opendir(path, fileHandleConsumer), originalFileHandleConsumer, path);
	}

	@Override
	public FuseResult read(String path, ByteBuffer buffer, long size, long offset, FileHandle fileHandle) {
		return logWithData("read(%s,*,%d,%d,%d)", () -> delegate.read(path, buffer, size, offset, fileHandle), buffer, path, size, offset, fileHandle.getAsLong());
	}

	@Override
	public FuseResult readdir(String path, Consumer<String> filler) {
		return log("readdir(%s)", () -> delegate.readdir(path, filler), path);
	}

	@Override
	public FuseResult release(String path, FileHandle fileHandle) {
		return log("release(%s,%d)", () -> delegate.release(path, fileHandle), path, fileHandle.getAsLong());
	}

	@Override
	public FuseResult releasedir(String path, FileHandle fileHandle) {
		return log("releasedir(%s,%d)", () -> delegate.releasedir(path, fileHandle), path, fileHandle.getAsLong());
	}

	@Override
	public FuseResult rename(String path, String newPath) {
		return log("rename(%s,%s)", () -> delegate.rename(path, newPath), path, newPath);
	}

	@Override
	public FuseResult rmdir(String path) {
		return log("rmdir(%s)", () -> delegate.rmdir(path), path);
	}

	@Override
	public FuseResult statfs(String path, FilesystemStats stats) {
		return log("statfs(%s,*)", () -> delegate.statfs(path, stats), path);
	}

	@Override
	public FuseResult truncate(String path, long offset) {
		return log("truncate(%s,%d)", () -> delegate.truncate(path, offset), path, offset);
	}

	@Override
	public FuseResult unlink(String path) {
		return log("unlink(%s)", () -> delegate.unlink(path), path);
	}

	@Override
	public FuseResult utimens(String path, Times times) {
		return log("utimens(%s,*)", () -> delegate.utimens(path, times), path, times);
	}

	@Override
	public FuseResult write(String path, ByteBuffer buffer, long bufSize, long writeOffset, FileHandle fileHandle) {
		return logWithData("write(%s,*,%d,%d,%d)", () -> delegate.write(path, buffer, bufSize, writeOffset, fileHandle), buffer, path, bufSize, writeOffset, fileHandle.getAsLong());
	}

	private FuseResult logWithHandle(String message, Function<WritableFileHandle, FuseResult> operation, WritableFileHandle originalFileHandleConsumer, Object... args) {
		ReadWriteFileHandle fileHandleConsumer = new ReadWriteFileHandle();
		FuseResult result = operation.apply(fileHandleConsumer);
		originalFileHandleConsumer.accept(fileHandleConsumer.getAsLong());
		if (result != StandardFuseResult.SUCCESS || logSuccess) {
			fuseLogger.debug(format(messageWithResult(message) + " handle:" + fileHandleConsumer.getAsLong(), argsWithResult(args, result)));
		}
		return result;
	}

	private FuseResult logWithData(String message, Supplier<FuseResult> operation, ByteBuffer buffer, Object... args) {
		if (logData) {
			FuseResult result = operation.get();
			if (result != StandardFuseResult.SUCCESS || logSuccess) {
				fuseLogger.debug(format(messageWithResult(message), argsWithResult(args, result))  + " " + toString(buffer.asReadOnlyBuffer()));
			}
			return result;
		} else {
			return log(message, operation, args);
		}
	}

	private FuseResult log(String message, Supplier<FuseResult> operation, Object... args) {
		FuseResult result = operation.get();
		if (result != StandardFuseResult.SUCCESS || logSuccess) {
			fuseLogger.debug(format(messageWithResult(message), argsWithResult(args, result)));
		}
		return result;
	}

	private String toString(ByteBuffer filledBuffer) {
		filledBuffer.flip();
		byte[] bytes = new byte[min(filledBuffer.limit(), 32)];
		filledBuffer.get(bytes);
		if (filledBuffer.remaining() > 0) {
			return format("data:%s... ascii:%s...", bytesToHex(bytes), decodeAscii(bytes));
		} else {
			return format("data:%s ascii:%s", bytesToHex(bytes), decodeAscii(bytes));
		}
	}

	private String decodeAscii(byte[] bytes) {
		return US_ASCII.decode(ByteBuffer.wrap(bytes)).toString();
	}

	private String messageWithResult(String message) {
		return message + ":%s";
	}

	private Object[] argsWithResult(Object[] args, FuseResult result) {
		Object[] argsWithResult = copyOf(args, args.length + 1);
		argsWithResult[args.length] = result;
		return argsWithResult;
	}

	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

}
