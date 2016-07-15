package org.cryptomator.frontend.fuse.impl;

import static org.cryptomator.common.test.matcher.PrivateFieldMatcher.hasField;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import javax.inject.Provider;

import org.cryptomator.frontend.fuse.api.FuseOperations;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

public class FuseOperationsFactoryTest {

	@SuppressWarnings("unchecked")
	private Provider<OpenFiles> openFilesProvider = mock(Provider.class);
	private OpenFiles openFiles = mock(OpenFiles.class);
	private NioAccess nioAccess = mock(NioAccess.class);
	private Logger fuseLogger = mock(Logger.class);
	private GetattrCacheFactory getattrCacheFactory = mock(GetattrCacheFactory.class);
	private GetattrCache getattrCache = mock(GetattrCache.class);
	
	private FuseOperationsFactory inTest = new FuseOperationsFactory(openFilesProvider, nioAccess, fuseLogger, getattrCacheFactory);
	
	@Before
	public void setUp() {
		when(openFilesProvider.get()).thenReturn(openFiles);
		when(getattrCacheFactory.create(any())).thenReturn(getattrCache);
	}
	
	@Test
	public void testNewNioFuseOperationsCreatesNioFuseOperations() {
		Path path = mock(Path.class);
		
		NioFuseOperations result = inTest.newNioFuseOperations(path);
		
		assertThat(result, hasField("root", Path.class).that(is(path)));
		assertThat(result, hasField("openFiles", OpenFiles.class).that(is(openFiles)));
		assertThat(result, hasField("nioAccess", NioAccess.class).that(is(nioAccess)));
		assertThat(result, hasField("getattrCache", GetattrCache.class).that(is(getattrCache)));
	}
	
	@Test
	public void testNewExceptionHandlingFuseOperationsDecoratorCreatesExceptionHandlingFuseOperationsDecorator() {
		FuseOperations delegate = mock(FuseOperations.class);
		
		ExceptionHandlingFuseOperationsDecorator result = inTest.newExceptionHandlingFuseOperationsDecorator(delegate);
		
		assertThat(result, hasField("delegate", FuseOperations.class).that(is(delegate)));
		assertThat(result, hasField("fuseLogger", Logger.class).that(is(fuseLogger)));
	}
	
	@Test
	public void testNewLoggingFuseOperationsDecoratorCreatesLoggingFuseOperationsDecorator() {
		FuseOperations delegate = mock(FuseOperations.class);
		
		LoggingFuseOperationsDecorator result = inTest.newLoggingFuseOperationsDecorator(delegate);
		
		assertThat(result, hasField("delegate", FuseOperations.class).that(is(delegate)));
		assertThat(result, hasField("fuseLogger", Logger.class).that(is(fuseLogger)));
	}
	
}
