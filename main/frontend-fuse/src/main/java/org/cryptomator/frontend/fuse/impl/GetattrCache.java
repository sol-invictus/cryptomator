package org.cryptomator.frontend.fuse.impl;

import java.nio.file.Path;

interface GetattrCache {

	GetattrResult reload(Path path);
	
	GetattrResult get(Path path);
	
}
