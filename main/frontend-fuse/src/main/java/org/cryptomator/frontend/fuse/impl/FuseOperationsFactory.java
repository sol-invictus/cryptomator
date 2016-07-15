package org.cryptomator.frontend.fuse.impl;

import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.cryptomator.frontend.fuse.FuseLogger;
import org.cryptomator.frontend.fuse.api.FuseOperations;
import org.slf4j.Logger;

@Singleton
class FuseOperationsFactory {

	private final Provider<OpenFiles> openFilesProvider;
	private final Logger fuseLogger;
	private final NioAccess nioAccess;
	private final GetattrCacheFactory getattrCacheFactory;
	
	@Inject
	public FuseOperationsFactory(Provider<OpenFiles> openFilesProvider, NioAccess nioAccess, @FuseLogger Logger fuseLogger, GetattrCacheFactory getattrCacheFactory) {
		this.openFilesProvider = openFilesProvider;
		this.fuseLogger = fuseLogger;
		this.getattrCacheFactory = getattrCacheFactory;
		this.nioAccess = nioAccess;
	}
	
	public NioFuseOperations newNioFuseOperations(Path path) {
		return new NioFuseOperations(path, nioAccess, openFilesProvider.get(), getattrCacheFactory);
	}

	public ExceptionHandlingFuseOperationsDecorator newExceptionHandlingFuseOperationsDecorator(FuseOperations delegate) {
		return new ExceptionHandlingFuseOperationsDecorator(delegate, fuseLogger);
	}

	public LoggingFuseOperationsDecorator newLoggingFuseOperationsDecorator(FuseOperations delegate) {
		return new LoggingFuseOperationsDecorator(delegate, fuseLogger);
	}
	
}
