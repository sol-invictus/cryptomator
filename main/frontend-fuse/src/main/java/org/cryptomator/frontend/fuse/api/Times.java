package org.cryptomator.frontend.fuse.api;

public interface Times {

	void accessTime(long epochMilliseconds);

	void modificationTime(long epochMilliseconds);

}
