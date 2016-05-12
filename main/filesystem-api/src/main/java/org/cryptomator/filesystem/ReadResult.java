package org.cryptomator.filesystem;

/**
 * Result of a read operation.
 */
public enum ReadResult {

	/**
	 * The end of file has not been reached while filling the buffer for the read operation.
	 */
	NO_EOF,

	/**
	 * The end of file has been reached while filling the buffer for the read operation. But at least one byte was read before the end of the file.
	 */
	EOF_REACHED,

	/**
	 * The end of file has been immediately reached. No data was read.
	 */
	EOF

}
