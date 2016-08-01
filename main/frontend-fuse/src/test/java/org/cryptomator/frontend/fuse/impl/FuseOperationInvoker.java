package org.cryptomator.frontend.fuse.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import org.cryptomator.frontend.fuse.api.Attributes;
import org.cryptomator.frontend.fuse.api.FileHandle;
import org.cryptomator.frontend.fuse.api.FilesystemStats;
import org.cryptomator.frontend.fuse.api.FuseOperations;
import org.cryptomator.frontend.fuse.api.FuseResult;
import org.cryptomator.frontend.fuse.api.Times;
import org.cryptomator.frontend.fuse.api.WritableFileHandle;

class FuseOperationInvoker {
	
	private static final String A_STRING = "aString";
	private static final String ANOTHER_STRING = "anotherString";
	private static final Attributes AN_ATTRIBUTES = mock(Attributes.class);
	private static final FileHandle A_FILE_HANDLE = mock(FileHandle.class);
	private static final int AN_INT = 432;
	private static final int ANOTHER_INT = 382;
	private static final WritableFileHandle A_WRITABLE_FILE_HANDLE = mock(WritableFileHandle.class);
	private static final Times A_TIMES = mock(Times.class);
	private static final FilesystemStats A_STATS = mock(FilesystemStats.class);
	private static final boolean A_BOOLEAN = true;
	private static final ByteBuffer A_BUFFER = ByteBuffer.allocate(12);
	private static final Consumer<String> A_STRING_CONSUMER = s -> {};
	
	public static final Collection<FuseOperationInvoker> ALL = unmodifiableCollection(asList(
			new FuseOperationInvoker(operations -> operations.access(A_STRING), "access"),
			new FuseOperationInvoker(operations -> operations.create(A_STRING, A_WRITABLE_FILE_HANDLE), "create"),
			new FuseOperationInvoker(operations -> operations.fgetattr(A_STRING, AN_ATTRIBUTES, A_FILE_HANDLE), "fgetattr"),
			new FuseOperationInvoker(operations -> operations.flush(A_STRING, A_FILE_HANDLE), "flush"),
			new FuseOperationInvoker(operations -> operations.fsync(A_STRING, A_BOOLEAN, A_FILE_HANDLE), "fsync"),
			new FuseOperationInvoker(operations -> operations.fsyncdir(A_STRING, A_BOOLEAN, A_FILE_HANDLE), "fsyncdir"),
			new FuseOperationInvoker(operations -> operations.ftruncate(A_STRING, AN_INT, A_FILE_HANDLE), "ftruncate"),
			new FuseOperationInvoker(operations -> operations.getattr(A_STRING, AN_ATTRIBUTES), "getattr"),
			new FuseOperationInvoker(operations -> operations.lock(A_STRING), "lock"),
			new FuseOperationInvoker(operations -> operations.mkdir(A_STRING), "mkdir"),
			new FuseOperationInvoker(operations -> operations.open(A_STRING, A_WRITABLE_FILE_HANDLE), "open"),
			new FuseOperationInvoker(operations -> operations.opendir(A_STRING, A_WRITABLE_FILE_HANDLE), "opendir"),
			new FuseOperationInvoker(operations -> operations.read(A_STRING, A_BUFFER, AN_INT, ANOTHER_INT, A_FILE_HANDLE), "read"),
			new FuseOperationInvoker(operations -> operations.readdir(A_STRING, A_STRING_CONSUMER), "readdir"),
			new FuseOperationInvoker(operations -> operations.release(A_STRING, A_FILE_HANDLE), "release"),
			new FuseOperationInvoker(operations -> operations.releasedir(A_STRING, A_FILE_HANDLE), "releasedir"),
			new FuseOperationInvoker(operations -> operations.rename(A_STRING, ANOTHER_STRING), "rename"),
			new FuseOperationInvoker(operations -> operations.rmdir(A_STRING), "rmdir"),
			new FuseOperationInvoker(operations -> operations.statfs(A_STRING, A_STATS), "statfs"),
			new FuseOperationInvoker(operations -> operations.truncate(A_STRING, AN_INT), "truncate"),
			new FuseOperationInvoker(operations -> operations.unlink(A_STRING), "unlink"),
			new FuseOperationInvoker(operations -> operations.utimens(A_STRING, A_TIMES), "utimens"),
			new FuseOperationInvoker(operations -> operations.write(A_STRING, A_BUFFER, AN_INT, ANOTHER_INT, A_FILE_HANDLE), "write")));
	
	private final Function<FuseOperations,FuseResult> invoker;
	private final String methodName;
	
	public FuseOperationInvoker(Function<FuseOperations,FuseResult> invoker, String methodName) {
		this.invoker = invoker;
		this.methodName = methodName;
	}
	
	public Function<FuseOperations,FuseResult> invoker() {
		return invoker;
	}
	
	public String operationName() {
		return methodName;
	}
	
	@Override
	public String toString() {
		return methodName;
	}
	
}