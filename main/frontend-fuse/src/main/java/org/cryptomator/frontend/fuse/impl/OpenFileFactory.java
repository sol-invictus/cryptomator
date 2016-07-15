package org.cryptomator.frontend.fuse.impl;

import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
class OpenFileFactory {
	
	private final NioAccess nioAccess;
	
	@Inject
	public OpenFileFactory(NioAccess nioAccess) {
		this.nioAccess = nioAccess;
	}
	
	public OpenFile newOpenFile(Path path) {
		return new OpenFile(path, nioAccess);
	}

}
