/*******************************************************************************
 * Copyright (c) 2015 Markus Kreusch
 * This file is licensed under the terms of the MIT license.
 * See the LICENSE.txt file for more info.
 ******************************************************************************/
package org.cryptomator.filesystem;

import static org.cryptomator.filesystem.CreateMode.CREATE_IF_MISSING;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A {@link File} in a {@link FileSystem}.
 *
 * @author Markus Kreusch
 */
public interface File extends Node, Comparable<File> {

	/**
	 * <p>
	 * Creates a new readable view of this {@code File}.
	 * <p>
	 * Multiple {@link ReadableFile Readable-} and {@link WritableFiles} can exist and be used for a single file at the same time.
	 * The file remains opened until all of these have been closed.
	 * <p>
	 * Instances returned by this method are safe to be used by multiple threads.
	 *
	 * @return a {@link ReadableFile} to work with
	 * @throws UncheckedIOException
	 *             if an {@link IOException} occurs while opening the file, the
	 *             file does not exist or is a directory
	 */
	ReadableFile openReadable() throws UncheckedIOException;

	/**
	 * <p>
	 * Creates a new writable view of this {@code File}.
	 * <p>
	 * Multiple {@link ReadableFile Readable-} and {@link WritableFiles} can exist and be used for a single file at the same time.
	 * The file remains opened until all of these have been closed.
	 * <p>
	 * Instances returned by this method are safe to be used by multiple threads.
	 *
	 * @return a {@link WritableFile} to work with
	 * @throws UncheckedIOException
	 *             if an {@link IOException} occurs while opening the file or
	 *             the file is a directory
	 */
	WritableFile openWritable(CreateMode mode) throws UncheckedIOException;

	/**
	 * Shortcut for {@code openWritable(CREATE_IF_MISSING)}.
	 *
	 * @see #openWritable(CreateMode)
	 * @see CreateMode#CREATE_IF_MISSING
	 */
	default WritableFile openWritable() throws UncheckedIOException {
		return openWritable(CREATE_IF_MISSING);
	}

	default void copyTo(File destination) {
		Copier.copy(this, destination);
	}

	/**
	 * Moves this file including content to a new location specified by <code>destination</code>.
	 */
	void moveTo(File destination) throws UncheckedIOException;

}
