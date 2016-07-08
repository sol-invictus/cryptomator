package org.cryptomator.frontend.fuse.api;

import java.io.File;

public interface FuseFilesystem {

	default String getName() {
		return "cryptomator";
	};

	default void init() {
	};

	default void beforeMount(File mountPoint) {
	};

	default void afterUnmount(File mountPoint) {
	};

	default void destroy() {
	};

	FuseOperations operations();

}
