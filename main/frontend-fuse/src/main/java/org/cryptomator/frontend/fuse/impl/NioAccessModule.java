package org.cryptomator.frontend.fuse.impl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class NioAccessModule {

	@Provides
	@Singleton
	public static NioAccess provideNioAccess() {
		return new NioAccessImpl();
	}

}
