package org.cryptomator.frontend.fuse.fusejnaadapter;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.cryptomator.frontend.fuse.FuseLogger;
import org.slf4j.Logger;

import net.fusejna.FuseFilesystem;

@Singleton
public class FuseFilesystemToFuseJnaAdapterFactory {

	private final Logger fuseLogger;
	
	@Inject
	FuseFilesystemToFuseJnaAdapterFactory(@FuseLogger Logger fuseLogger) {
		this.fuseLogger = fuseLogger;
	}
	
	public FuseFilesystem create(org.cryptomator.frontend.fuse.api.FuseFilesystem delegate) {
		return new FuseFilesystemToFuseJnaAdapter(delegate, fuseLogger);
	}
	
}
