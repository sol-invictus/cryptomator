package org.cryptomator.frontend.fuse.api;

public interface Attributes extends Times {

	void file();

	void folder();

	void size(long size);

	void creationTime(long epochMilliseconds);

}
