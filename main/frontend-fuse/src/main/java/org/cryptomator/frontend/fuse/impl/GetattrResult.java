package org.cryptomator.frontend.fuse.impl;

import java.nio.file.attribute.BasicFileAttributes;

import org.cryptomator.frontend.fuse.api.Attributes;

class GetattrResult {

	private final static long PATH_IS_FOLDER = -1;
	
	private final long accessTime;
	private final long creationTime;
	private final long modificationTime;
	private final long fileSize;
	
	public GetattrResult(BasicFileAttributes fileAttributes) {
		if (fileAttributes.isRegularFile()) {
			fileSize = fileAttributes.size();
		} else {
			fileSize = PATH_IS_FOLDER;
		}
		accessTime = fileAttributes.lastAccessTime().toMillis();
		creationTime = fileAttributes.creationTime().toMillis();
		modificationTime = fileAttributes.lastModifiedTime().toMillis();
	}
	
	public void fill(Attributes attributes) {
		attributes.accessTime(accessTime);
		attributes.creationTime(creationTime);
		attributes.modificationTime(modificationTime);
		if (fileSize == PATH_IS_FOLDER) {
			attributes.folder();
		} else {
			attributes.file();
			attributes.size(fileSize);
		}
	}
	
}
