package org.cryptomator.frontend.fuse;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {NioAccessModule.class, FuseLoggerModule.class})
public interface FuseFrontendComponent {

	FuseFrontendFactory fuseFrontendFactory();
	
}
