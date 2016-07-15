package org.cryptomator.frontend.fuse.impl;

import static org.cryptomator.frontend.fuse.api.StandardFuseResult.FILE_DOES_NOT_EXIST;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.IO_ERROR;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.NoSuchFileException;

import org.cryptomator.frontend.fuse.api.FuseOperations;
import org.cryptomator.frontend.fuse.api.FuseResult;
import org.junit.After;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

@RunWith(Theories.class)
public class ExceptionHandlingFuseOperationsDecoratorTest {

private static final String TEST_MESSAGE = "TestMessage";
	
	private FuseOperations delegate = mock(FuseOperations.class);

	private Logger logger = mock(Logger.class);

	private FuseOperations inTest = new ExceptionHandlingFuseOperationsDecorator(delegate, logger);
	
	@DataPoints
	public static Iterable<FuseOperationInvoker> INVOKERS = FuseOperationInvoker.ALL;
	
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
	}
		
	@Theory
	public void testUncheckedIOExceptionIsLoggedUsingTraceIfEnabled(FuseOperationInvoker invoker) {
		UncheckedIOException exception = new UncheckedIOException(new IOException(TEST_MESSAGE));
		when(invoker.invoker().apply(delegate)).thenThrow(exception);
		when(logger.isTraceEnabled()).thenReturn(true);
		
		invoker.invoker().apply(inTest);
		
		verify(logger).trace(argThat(containsString(invoker.operationName())), eq(exception));
	}
	
	@Theory
	public void testUncheckedIOExceptionIsNotLoggedIfTraceDisabled(FuseOperationInvoker invoker) {
		UncheckedIOException exception = new UncheckedIOException(new IOException(TEST_MESSAGE));
		when(invoker.invoker().apply(delegate)).thenThrow(exception);
		when(logger.isInfoEnabled()).thenReturn(true);
		
		invoker.invoker().apply(inTest);
		
		verify(logger, never()).trace(any(), any(Throwable.class));
	}
		
	@Theory
	public void testRuntimeExceptionIsLoggedUsingInfoIfEnabled(FuseOperationInvoker invoker) {
		RuntimeException exception = new RuntimeException(TEST_MESSAGE);
		when(invoker.invoker().apply(delegate)).thenThrow(exception);
		when(logger.isTraceEnabled()).thenReturn(true);
		when(logger.isInfoEnabled()).thenReturn(true);
		
		invoker.invoker().apply(inTest);
		
		verify(logger).info(argThat(containsString(invoker.operationName())), eq(exception));
	}
	
	@Theory
	public void testRuntimeExceptionIsLoggedUsingErrorIfInfoDisabled(FuseOperationInvoker invoker) {
		RuntimeException exception = new RuntimeException(TEST_MESSAGE);
		when(invoker.invoker().apply(delegate)).thenThrow(exception);
		when(logger.isTraceEnabled()).thenReturn(true);
		when(logger.isInfoEnabled()).thenReturn(false);
		
		invoker.invoker().apply(inTest);
		
		verify(logger).error(argThat(allOf(containsString(TEST_MESSAGE),containsString(invoker.operationName()))));
	}
	
	@Theory
	public void testUncheckedIOExceptionLeadsToIoError(FuseOperationInvoker invoker) {
		UncheckedIOException exception = new UncheckedIOException(new IOException(TEST_MESSAGE));
		when(invoker.invoker().apply(delegate)).thenThrow(exception);
		when(logger.isTraceEnabled()).thenReturn(true);
		
		assertThat(invoker.invoker().apply(inTest), is(IO_ERROR));
	}
	
	@Theory
	public void testRuntimeExceptionLeadsToIoError(FuseOperationInvoker invoker) {
		RuntimeException exception = new RuntimeException(TEST_MESSAGE);
		when(invoker.invoker().apply(delegate)).thenThrow(exception);
		when(logger.isTraceEnabled()).thenReturn(true);
		
		assertThat(invoker.invoker().apply(inTest), is(IO_ERROR));
	}
	
	@Theory
	public void testFileNotFoundExceptionLeadsToFileDoesNotExist(FuseOperationInvoker invoker) {
		UncheckedIOException exception = new UncheckedIOException(new FileNotFoundException(TEST_MESSAGE));
		when(invoker.invoker().apply(delegate)).thenThrow(exception);
		when(logger.isTraceEnabled()).thenReturn(true);
		
		assertThat(invoker.invoker().apply(inTest), is(FILE_DOES_NOT_EXIST));
	}
	
	@Theory
	public void testNoSuchFileExceptionLeadsToFileDoesNotExist(FuseOperationInvoker invoker) {
		UncheckedIOException exception = new UncheckedIOException(new NoSuchFileException(TEST_MESSAGE));
		when(invoker.invoker().apply(delegate)).thenThrow(exception);
		when(logger.isTraceEnabled()).thenReturn(true);
		
		assertThat(invoker.invoker().apply(inTest), is(FILE_DOES_NOT_EXIST));
	}

}

