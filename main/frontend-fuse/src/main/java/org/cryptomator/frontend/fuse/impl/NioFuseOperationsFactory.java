package org.cryptomator.frontend.fuse.impl;

import static java.util.Arrays.asList;

import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.cryptomator.frontend.fuse.api.FuseOperations;

@Singleton
public class NioFuseOperationsFactory {

	private final FuseOperationsFactory factory;
	
	@Inject
	NioFuseOperationsFactory(FuseOperationsFactory factory) {
		this.factory = factory;
	}
	
	public FuseOperations create(Path root, Flag ... flags) {
		FuseOperations nioFuseOperations = factory.newNioFuseOperations(root);
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
		return factory.newExceptionHandlingFuseOperationsDecorator(operations);
	}

	private FuseOperations decorateWithDataLogging(FuseOperations operations) {
		LoggingFuseOperationsDecorator decorator = decorateWithOperationLogging(operations);
		decorator.setLogData(true);
		return decorator;
	}

	private LoggingFuseOperationsDecorator decorateWithOperationLogging(FuseOperations operations) {
		return factory.newLoggingFuseOperationsDecorator(operations);
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
