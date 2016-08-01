package org.cryptomator.frontend.fuse.api;


public class ReadWriteFileHandle implements FileHandle,WritableFileHandle {

	private long handle = -1;
	
	@Override
	public long getAsLong() {
		return handle;
	}

	@Override
	public void accept(long value) {
		handle = value;
	}
	
}