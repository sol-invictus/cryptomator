package org.cryptomator.frontend.fuse;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class FuseLoggerModule {

	private static final Logger LOG = LoggerFactory.getLogger(FuseLoggerModule.class);

	@Provides
	@Singleton
	@FuseLogger
	public static Logger provideFuseLogger() {
		return LOG;
	}

}
