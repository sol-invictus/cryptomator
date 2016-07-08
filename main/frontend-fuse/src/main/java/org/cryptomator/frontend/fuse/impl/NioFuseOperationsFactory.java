package org.cryptomator.frontend.fuse.impl;

import static java.util.Arrays.asList;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.cryptomator.frontend.fuse.FuseLogger;
import org.cryptomator.frontend.fuse.api.FuseOperations;
import org.slf4j.Logger;

@Singleton
public class NioFuseOperationsFactory {

	private final Provider<OpenFiles> openFilesProvider;
	private final Logger fuseLogger;
	private final NioAccess nioAccess;
	private final GetattrCacheFactory getattrCacheFactory;
	
	@Inject
	NioFuseOperationsFactory(Provider<OpenFiles> openFilesProvider, NioAccess nioAccess, @FuseLogger Logger fuseLogger, GetattrCacheFactory getattrCacheFactory) {
		this.openFilesProvider = openFilesProvider;
		this.fuseLogger = fuseLogger;
		this.getattrCacheFactory = getattrCacheFactory;
		this.nioAccess = nioAccess;
	}
	
	public FuseOperations create(Path root, Flag ... flags) {
		FuseOperations nioFuseOperations = new NioFuseOperations(root, nioAccess, openFilesProvider.get(), getattrCacheFactory);
		return decorate(nioFuseOperations, asSet(flags));
	}

	private FuseOperations decorate(FuseOperations operations, Set<Flag> flags) {
		if (flags.contains(Flag.LOG_DATA)) {
			return decorateWithDataLogging(decorateWithExceptionHandling(operations));
		} else if (flags.contains(Flag.LOG_OPERATIONS)) {
			return decorateWithOperationLogging(decorateWithExceptionHandling(operations));
		} else {
			return decorateWithExceptionHandling(operations);
		}
	}

	private ExceptionHandlingFuseOperationsDecorator decorateWithExceptionHandling(FuseOperations operations) {
		return new ExceptionHandlingFuseOperationsDecorator(operations, fuseLogger);
	}

	private FuseOperations decorateWithDataLogging(FuseOperations operations) {
		LoggingFuseOperationsDecorator decorator = decorateWithOperationLogging(operations);
		decorator.setLogData(true);
		return decorator;
	}

	private LoggingFuseOperationsDecorator decorateWithOperationLogging(FuseOperations operations) {
		return new LoggingFuseOperationsDecorator(operations, fuseLogger);
	}

	private Set<Flag> asSet(Flag... flags) {
		Set<Flag> flagsAsSet = EnumSet.noneOf(Flag.class);
		flagsAsSet.addAll(asList(flags));
		return flagsAsSet;
	}
	
	public enum Flag {
		LOG_OPERATIONS,
		LOG_DATA
	}
	
}
