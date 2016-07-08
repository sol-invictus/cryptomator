package org.cryptomator.frontend.fuse.impl;

import static org.cryptomator.frontend.fuse.api.StandardFuseResult.IO_ERROR;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.SUCCESS;

import java.nio.ByteBuffer;
import java.nio.file.Path;

import org.cryptomator.frontend.fuse.api.FuseResult;

class OpenFile {

	private final Path path;
	
	public OpenFile(Path path) {
		this.path = path;
	}

	public boolean hasPath(Path path) {
		return this.path.equals(path);
	}

	public Path path() {
		return path;
	}

	public FuseResult read(ByteBuffer buffer, long size, long offset) {
		// TODO Auto-generated method stub
		return IO_ERROR;
	}

	public FuseResult write(ByteBuffer buffer, long size, long offset) {
		// TODO Auto-generated method stub
		return IO_ERROR;
	}

	public FuseResult flush() {
		// TODO Auto-generated method stub
		return SUCCESS;
	}

	public FuseResult fsync(boolean flushMetadata) {
		// TODO Auto-generated method stub
		return SUCCESS;
	}

	public void release() {
		
	}

	public FuseResult truncate(long offset) {
		// TODO Auto-generated method stub
		return IO_ERROR;
	}

}
