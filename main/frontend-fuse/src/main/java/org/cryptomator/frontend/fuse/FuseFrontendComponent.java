package org.cryptomator.frontend.fuse;

import javax.inject.Singleton;

import org.cryptomator.frontend.fuse.impl.NioAccessModule;

import dagger.Component;

@Singleton
@Component(modules = {NioAccessModule.class, FuseLoggerModule.class})
public interface FuseFrontendComponent {

	FuseFrontendFactory fuseFrontendFactory();
	
}
