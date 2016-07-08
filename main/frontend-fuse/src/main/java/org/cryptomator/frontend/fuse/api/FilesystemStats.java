package org.cryptomator.frontend.fuse.api;

public interface FilesystemStats {

	void available(long availableBytes);

	void used(long usedBytes);

}
