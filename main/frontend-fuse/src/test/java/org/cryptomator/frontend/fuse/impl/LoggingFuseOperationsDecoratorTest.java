package org.cryptomator.frontend.fuse.impl;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.cryptomator.frontend.fuse.api.FileHandle;
import org.cryptomator.frontend.fuse.api.FuseOperations;
import org.cryptomator.frontend.fuse.api.FuseResult;
import org.cryptomator.frontend.fuse.api.WritableFileHandle;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

@RunWith(Theories.class)
public class LoggingFuseOperationsDecoratorTest {
	
	private static final Set<String> NAMES_OF_OPERATIONS_WITH_FILE_HANDLE = new HashSet<>(asList("open","opendir","create"));
	
	private static final Predicate<FuseOperationInvoker> ALL_EXCEPT_OPERATINS_WITH_FILE_HANDLE = invoker -> {
		return !NAMES_OF_OPERATIONS_WITH_FILE_HANDLE.contains(invoker.operationName());
	};

	private FuseOperations delegate = mock(FuseOperations.class);

	private Logger logger = mock(Logger.class);

	private LoggingFuseOperationsDecorator inTest = new LoggingFuseOperationsDecorator(delegate, logger);
	
	@DataPoints
	public static Iterable<FuseOperationInvoker> INVOKERS = FuseOperationInvoker.ALL.stream().filter(ALL_EXCEPT_OPERATINS_WITH_FILE_HANDLE).collect(toList());
	
	@After
	public void tearDown() {
		validateMockitoUsage();
	}
	
	@Theory
	public void testMethodDelegatesToDelegate(FuseOperationInvoker invoker) {
		FuseResult expectedResult = mock(FuseResult.class);
		when(invoker.invoker().apply(delegate)).thenReturn(expectedResult);
		
		FuseResult result = invoker.invoker().apply(inTest);
		
		assertThat(result, is(expectedResult));
		verify(logger).debug(argThat(containsString(invoker.operationName())));
	}
	
	@Test
	public void testCreateDelegatesToDelegate() {
		String aString = "fooBar";
		long handle = 47382;
		FuseResult expectedResult = mock(FuseResult.class);
		WritableFileHandle writableHandle = mock(WritableFileHandle.class);
		when(delegate.create(eq(aString), any())).thenAnswer(invocation -> {
			invocation.getArgumentAt(1, WritableFileHandle.class).accept(handle);
			return expectedResult;
		});
		
		FuseResult result = inTest.create(aString, writableHandle);
		
		assertThat(result, is(expectedResult));
		verify(writableHandle).accept(handle);
		verify(logger).debug(argThat(containsString("create")));
	}
	
	@Test
	public void testOpenDelegatesToDelegate() {
		String aString = "fooBar";
		long handle = 47382;
		FuseResult expectedResult = mock(FuseResult.class);
		WritableFileHandle writableHandle = mock(WritableFileHandle.class);
		when(delegate.open(eq(aString), any())).thenAnswer(invocation -> {
			invocation.getArgumentAt(1, WritableFileHandle.class).accept(handle);
			return expectedResult;
		});
		
		FuseResult result = inTest.open(aString, writableHandle);
		
		assertThat(result, is(expectedResult));
		verify(writableHandle).accept(handle);
		verify(logger).debug(argThat(containsString("open")));
	}
	
	@Test
	public void testOpendirDelegatesToDelegate() {
		String aString = "fooBar";
		long handle = 47382;
		FuseResult expectedResult = mock(FuseResult.class);
		WritableFileHandle writableHandle = mock(WritableFileHandle.class);
		when(delegate.opendir(eq(aString), any())).thenAnswer(invocation -> {
			invocation.getArgumentAt(1, WritableFileHandle.class).accept(handle);
			return expectedResult;
		});
		
		FuseResult result = inTest.opendir(aString, writableHandle);
		
		assertThat(result, is(expectedResult));
		verify(writableHandle).accept(handle);
		verify(logger).debug(argThat(containsString("opendir")));
	}
	
	@Test
	public void testReadLogsDataIfLogDataIsEnabled() {
		String aPath = "fooBar";
		FileHandle aHandle = () -> 1;
		FuseResult aFuseResult = FuseResult.withValue(4);
		inTest.setLogData(true);
		ByteBuffer data = ByteBuffer.allocate(4);
		when(delegate.read(aPath, data, data.capacity(), 0, aHandle)).thenAnswer(new Answer<FuseResult>() {
			@Override
			public FuseResult answer(InvocationOnMock invocation) throws Throwable {
				ByteBuffer buffer = invocation.getArgumentAt(1, ByteBuffer.class);
				buffer.put(new byte[]{0,-2,16,-96});
				return aFuseResult;
			}
		});
		
		inTest.read(aPath, data, data.capacity(), 0, aHandle);
		
		verify(logger).debug(argThat(allOf(containsString("read"), containsString("00FE10A0"))));
	}
	
	@Test
	public void testReadDoesNotLogDataIfLogDataIsDisabled() {
		String aPath = "fooBar";
		FileHandle aHandle = () -> 1;
		FuseResult aFuseResult = FuseResult.withValue(4);
		inTest.setLogData(false);
		ByteBuffer data = ByteBuffer.allocate(4);
		when(delegate.read(aPath, data, data.capacity(), 0, aHandle)).thenAnswer(new Answer<FuseResult>() {
			@Override
			public FuseResult answer(InvocationOnMock invocation) throws Throwable {
				ByteBuffer buffer = invocation.getArgumentAt(1, ByteBuffer.class);
				buffer.put(new byte[]{0,-2,16,-96});
				return aFuseResult;
			}
		});
		
		inTest.read(aPath, data, data.capacity(), 0, aHandle);
		
		verify(logger).debug(argThat(allOf(containsString("read"), not(containsString("00FE10A0")))));
	}
	
	@Test
	public void testWriteLogsDataIfLogDataIsEnabled() {
		String aPath = "fooBar";
		FileHandle aHandle = () -> 1;
		inTest.setLogData(true);
		FuseResult aFuseResult = FuseResult.withValue(4);
		ByteBuffer data = ByteBuffer.wrap(new byte[]{0,-2,16,-96});
		when(delegate.write(aPath, data, data.capacity(), 0, aHandle)).thenAnswer(new Answer<FuseResult>() {
			@Override
			public FuseResult answer(InvocationOnMock invocation) throws Throwable {
				ByteBuffer buffer = invocation.getArgumentAt(1, ByteBuffer.class);
				buffer.get(new byte[4]);
				return aFuseResult;
			}
		});
		
		inTest.write(aPath, data, data.capacity(), 0, aHandle);
		
		verify(logger).debug(argThat(allOf(containsString("write"), containsString("00FE10A0"))));
	}
	
	@Test
	public void testWriteDoesNotLogDataIfLogDataIsDisabled() {
		String aPath = "fooBar";
		FileHandle aHandle = () -> 1;
		inTest.setLogData(false);
		FuseResult aFuseResult = FuseResult.withValue(4);
		ByteBuffer data = ByteBuffer.wrap(new byte[]{0,-2,16,-96});
		when(delegate.write(aPath, data, data.capacity(), 0, aHandle)).thenAnswer(new Answer<FuseResult>() {
			@Override
			public FuseResult answer(InvocationOnMock invocation) throws Throwable {
				ByteBuffer buffer = invocation.getArgumentAt(1, ByteBuffer.class);
				buffer.get(new byte[4]);
				return aFuseResult;
			}
		});
		
		inTest.write(aPath, data, data.capacity(), 0, aHandle);
		
		verify(logger).debug(argThat(allOf(containsString("write"), not(containsString("00FE10A0")))));
	}
	
}
