package org.cryptomator.filesystem;

import java.io.UncheckedIOException;

/**
 * Operation modes when opening a file for writing.
 */
public enum CreateMode {

	/**
	 * Creates the file if it is missing. Opens it if it is present.
	 */
	CREATE_IF_MISSING, //

	/**
	 * Creates a new file and fails with an {@link UncheckedIOException} if the file is present.
	 */
	CREATE_AND_FAIL_IF_PRESENT, //

	/**
	 * Fails with an {@link UncheckedIOException} if the file is missing.
	 */
	FAIL_IF_MISSING

}
