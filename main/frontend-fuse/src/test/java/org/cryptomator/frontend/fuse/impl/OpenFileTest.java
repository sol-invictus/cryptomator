package org.cryptomator.frontend.fuse.impl;

import static org.cryptomator.common.test.matcher.PrivateFieldMatcher.hasField;
import static org.cryptomator.common.test.matcher.ThrowableMatcher.throwableWithCause;
import static org.cryptomator.common.test.matcher.ThrowableMatcher.throwableWithCauseThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.cryptomator.common.test.matcher.ThrowableMatcher;
import org.cryptomator.frontend.fuse.api.FuseResult;
import org.cryptomator.frontend.fuse.api.StandardFuseResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class OpenFileTest {

	private Path path = mock(Path.class);
	private AsynchronousFileChannel channel = mock(AsynchronousFileChannel.class);
	
	private OpenFile inTest;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before
	public void setUp() throws IOException {
		NioAccess nioAccess = mock(NioAccess.class);
		when(nioAccess.openAsyncFileChannel(path, StandardOpenOption.READ, StandardOpenOption.WRITE)).thenReturn(channel);
		
		inTest = new OpenFile(path, nioAccess);
	}
	
	@After
	public void tearDown() {
		validateMockitoUsage();
	}
	
	@Test
	public void testConstructorWrapsIoExceptionInUncheckedIoException() throws IOException {
		NioAccess nioAccess = mock(NioAccess.class);
		IOException exception = new IOException();
		when(nioAccess.openAsyncFileChannel(path, StandardOpenOption.READ, StandardOpenOption.WRITE)).thenThrow(exception);
		
		thrown.expect(UncheckedIOException.class);
		thrown.expectCause(is(exception));
		
		new OpenFile(path, nioAccess);
	}
	
	@Test
	public void testPathReturnsPath() {
		assertThat(inTest.path(), is(path));
	}
	
	@Test
	public void testHasPathReturnsTrueForEqualPath() {
		assertTrue(inTest.hasPath(path));
	}
	
	@Test
	public void testHasPathReturnsFalseForNonEqualPath() {
		assertFalse(inTest.hasPath(mock(Path.class)));
	}
	
	@Test
	public void testOpenFileHasCorrectFields() {
		assertThat(inTest, hasField("path", Path.class).that(is(path)));
		assertThat(inTest, hasField("channel", AsynchronousFileChannel.class).that(is(channel)));
	}
		
	@Test
	public void testReadReturnsBytesRead() throws InterruptedException, ExecutionException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		long position = 3282;
		int bytesRead = 78473;
		@SuppressWarnings("unchecked")
		Future<Integer> future = mock(Future.class);
		when(channel.read(buffer, position)).thenReturn(future);
		when(future.get()).thenReturn(bytesRead);
		
		FuseResult result = inTest.read(buffer, 4, position);
		
		assertThat(result.getAsInt(), is(bytesRead));
	}
	
	@Test
	public void testReadReturnsZeroIfBytesReadIsMinusOne() throws InterruptedException, ExecutionException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		long position = 3282;
		@SuppressWarnings("unchecked")
		Future<Integer> future = mock(Future.class);
		when(channel.read(buffer, position)).thenReturn(future);
		when(future.get()).thenReturn(-1);
		
		FuseResult result = inTest.read(buffer, 4, position);
		
		assertThat(result.getAsInt(), is(0));
	}
	
	@Test
	public void testReadWrapsExecutionExceptionInIoAndUncheckedIoException() throws InterruptedException, ExecutionException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		long position = 3282;
		ExecutionException executionException = new ExecutionException(new Throwable());
		@SuppressWarnings("unchecked")
		Future<Integer> future = mock(Future.class);
		when(channel.read(buffer, position)).thenReturn(future);
		when(future.get()).thenThrow(executionException);
		
		thrown.expect(UncheckedIOException.class);
		thrown.expectCause(is(allOf(instanceOf(IOException.class),throwableWithCauseThat(is(executionException)))));
		
		inTest.read(buffer, 4, position);
	}
	
	@Test
	public void testReadWrapsInterruptedExceptionInIoAndUncheckedIoException() throws InterruptedException, ExecutionException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		long position = 3282;
		InterruptedException interruptedException = new InterruptedException();
		@SuppressWarnings("unchecked")
		Future<Integer> future = mock(Future.class);
		when(channel.read(buffer, position)).thenReturn(future);
		when(future.get()).thenThrow(interruptedException);
		
		thrown.expect(UncheckedIOException.class);
		thrown.expectCause(is(allOf(instanceOf(IOException.class),throwableWithCauseThat(is(interruptedException)))));
		
		inTest.read(buffer, 4, position);
	}
	
	@Test
	public void testWriteReturnsBytesWritten() throws InterruptedException, ExecutionException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		long position = 3282;
		int bytesWritten = 78473;
		@SuppressWarnings("unchecked")
		Future<Integer> future = mock(Future.class);
		when(channel.write(buffer, position)).thenReturn(future);
		when(future.get()).thenReturn(bytesWritten);
		
		FuseResult result = inTest.write(buffer, 4, position);
		
		assertThat(result.getAsInt(), is(bytesWritten));
	}
	
	@Test
	public void testWriteWrapsExecutionExceptionInIoAndUncheckedIoException() throws InterruptedException, ExecutionException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		long position = 3282;
		ExecutionException executionException = new ExecutionException(new Throwable());
		@SuppressWarnings("unchecked")
		Future<Integer> future = mock(Future.class);
		when(channel.write(buffer, position)).thenReturn(future);
		when(future.get()).thenThrow(executionException);
		
		thrown.expect(UncheckedIOException.class);
		thrown.expectCause(is(allOf(instanceOf(IOException.class),throwableWithCauseThat(is(executionException)))));
		
		inTest.write(buffer, 4, position);
	}
	
	@Test
	public void testWriteWrapsInterruptedExceptionInIoAndUncheckedIoException() throws InterruptedException, ExecutionException {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		long position = 3282;
		InterruptedException interruptedException = new InterruptedException();
		@SuppressWarnings("unchecked")
		Future<Integer> future = mock(Future.class);
		when(channel.write(buffer, position)).thenReturn(future);
		when(future.get()).thenThrow(interruptedException);
		
		thrown.expect(UncheckedIOException.class);
		thrown.expectCause(is(allOf(instanceOf(IOException.class),throwableWithCauseThat(is(interruptedException)))));
		
		inTest.write(buffer, 4, position);
	}
	
	@Test
	public void testFsyncInvokesForceWithTrue() throws IOException {
		FuseResult result = inTest.fsync(true);
		
		assertThat(result, is(StandardFuseResult.SUCCESS));
		verify(channel).force(true);
	}
	
	@Test
	public void testFsyncInvokesForceWithFalse() throws IOException {
		FuseResult result = inTest.fsync(false);
		
		assertThat(result, is(StandardFuseResult.SUCCESS));
		verify(channel).force(false);
	}
	
	@Test
	public void testFlushInvokesForceWithFalse() throws IOException {
		FuseResult result = inTest.flush();
		
		assertThat(result, is(StandardFuseResult.SUCCESS));
		verify(channel).force(false);
	}
	
	@Test
	public void testTruncateInvokesTruncate() throws IOException {
		long size = 34882;
		when(channel.size()).thenReturn(size);
		
		FuseResult result = inTest.truncate(size - 100);
		
		assertThat(result, is(StandardFuseResult.SUCCESS));
		verify(channel).truncate(size - 100);
	}
	
	@Test
	public void testTruncateReturnsCanNotGrowFileThroughTruncateIfSizeSmallerOffset() throws IOException {
		long size = 34882;
		when(channel.size()).thenReturn(size);
		
		FuseResult result = inTest.truncate(size + 100);
		
		assertThat(result, is(StandardFuseResult.CAN_NOT_GROW_FILE_THROUGH_TRUNCATE));
	}
	
	@Test
	public void testReleaseInvokesForceAndClose() throws IOException {
		inTest.release();
		
		verify(channel).force(true);
		verify(channel).close();
	}
	
	@Test
	public void testReleaseInvokesCloseEvenIfForceFails() throws IOException {
		IOException exception = new IOException();
		doThrow(exception).when(channel).force(true);
		
		try {
			inTest.release();
		} catch (UncheckedIOException e) {
			assertThat(e, is(throwableWithCause(exception)));
		}
		
		verify(channel).close();
	}
	
}
