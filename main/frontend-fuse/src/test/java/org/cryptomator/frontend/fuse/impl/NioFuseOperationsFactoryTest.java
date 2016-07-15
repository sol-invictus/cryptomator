package org.cryptomator.frontend.fuse.impl;

import static org.cryptomator.frontend.fuse.impl.NioFuseOperationsFactory.Flag.LOG_DATA;
import static org.cryptomator.frontend.fuse.impl.NioFuseOperationsFactory.Flag.LOG_OPERATIONS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.cryptomator.frontend.fuse.api.FuseOperations;
import org.junit.Test;

public class NioFuseOperationsFactoryTest {

	private FuseOperationsFactory factory = mock(FuseOperationsFactory.class);
	
	private Path aPath = mock(Path.class);
	
	private NioFuseOperationsFactory inTest = new NioFuseOperationsFactory(factory);
	
	@Test
	public void testCreateWithoutFlagsCreatesNioFuseOperationsWrappedInExceptionHandlingDecorator() {
		NioFuseOperations nioFuseOperations = mock(NioFuseOperations.class);
		ExceptionHandlingFuseOperationsDecorator exceptionHandlingDecorator = mock(ExceptionHandlingFuseOperationsDecorator.class);
		when(factory.newNioFuseOperations(aPath)).thenReturn(nioFuseOperations);
		when(factory.newExceptionHandlingFuseOperationsDecorator(nioFuseOperations)).thenReturn(exceptionHandlingDecorator);
		
		FuseOperations result = inTest.create(aPath);
		
		assertThat(result, is(exceptionHandlingDecorator));
	}
	
	@Test
	public void testCreateWithFlagLogDatasCreatesNioFuseOperationsWrappedInExceptionHandlingAndLoggingDecorators() {
		NioFuseOperations nioFuseOperations = mock(NioFuseOperations.class);
		ExceptionHandlingFuseOperationsDecorator exceptionHandlingDecorator = mock(ExceptionHandlingFuseOperationsDecorator.class);
		LoggingFuseOperationsDecorator loggingDecorator = mock(LoggingFuseOperationsDecorator.class);
		when(factory.newNioFuseOperations(aPath)).thenReturn(nioFuseOperations);
		when(factory.newExceptionHandlingFuseOperationsDecorator(nioFuseOperations)).thenReturn(exceptionHandlingDecorator);
		when(factory.newLoggingFuseOperationsDecorator(exceptionHandlingDecorator)).thenReturn(loggingDecorator);
		
		FuseOperations result = inTest.create(aPath, LOG_DATA);
		
		assertThat(result, is(loggingDecorator));
		verify(loggingDecorator).setLogData(true);
		
	}
	
	@Test
	public void testCreateWithFlagLogOperationsCreatesNioFuseOperationsWrappedInExceptionHandlingAndLoggingDecoratorsWithLogDataEnabled() {
		NioFuseOperations nioFuseOperations = mock(NioFuseOperations.class);
		ExceptionHandlingFuseOperationsDecorator exceptionHandlingDecorator = mock(ExceptionHandlingFuseOperationsDecorator.class);
		LoggingFuseOperationsDecorator loggingDecorator = mock(LoggingFuseOperationsDecorator.class);
		when(factory.newNioFuseOperations(aPath)).thenReturn(nioFuseOperations);
		when(factory.newExceptionHandlingFuseOperationsDecorator(nioFuseOperations)).thenReturn(exceptionHandlingDecorator);
		when(factory.newLoggingFuseOperationsDecorator(exceptionHandlingDecorator)).thenReturn(loggingDecorator);
		
		FuseOperations result = inTest.create(aPath, LOG_OPERATIONS);
		
		assertThat(result, is(loggingDecorator));
		verify(loggingDecorator, never()).setLogData(anyBoolean());
	}
	
}
