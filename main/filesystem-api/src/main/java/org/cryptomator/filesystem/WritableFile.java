/*******************************************************************************
 * Copyright (c) 2015 Markus Kreusch
 * This file is licensed under the terms of the MIT license.
 * See the LICENSE.txt file for more info.
 ******************************************************************************/
package org.cryptomator.filesystem;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

/**
 * <p>
 * A {@code WritableFile} allows to modify a {@link File}.
 * <p>
 * Instances can safely be used by multiple threads.
 * <p>
 * All operations on a {@code WritableFile} are atomic regarding other operations in {@link ReadableFile} and {@link File}.
 * <p>
 * Changes to a {@code WritableFile} may immediately be visible to corresponding {@code ReadableFile}s. Nevertheless a
 * {@link WritableFile} only guarantees that such changes are visible after a successful invocation of {@link #flush()} or
 * {@link #close()}.
 */
public interface WritableFile extends AutoCloseable {

	/**
	 * @return {@code true} if this WritableFile is still open. Otherwise
	 *         {@code false}.
	 */
	boolean isOpen() throws UncheckedIOException;

	/**
	 * Writes the data in the given {@link ByteBuffer} to this {@code WritableFile} at
	 * the given position.
	 * <p>
	 * If the position is set to a value greater than the current end of file
	 * data will be written to the given position. The value of all bytes
	 * between this position and the previous end of file will be unspecified.
	 * <p>
	 * The given {@code ByteBuffer} must have at least one byte remaining.
	 * <p>
	 * Data written by {@code WritableFile}s must only be visible to {@link ReadableFile}s for the same {@link java.io.File} after
	 * closing or flushing the {@code WritableFile}. Nevertheless data can be visible earlier. Each write operation is atomic regarding future reads.
	 *
	 * @param position
	 *            the position to start writing
	 * @param source
	 *            the byte buffer to use
	 * @throws UncheckedIOException
	 *             if an {@link IOException} occurs while writing or if this {@code WritableFile} is already closed
	 * @throws IllegalArgumentException
	 *             if the {@link ByteBuffer} has zero bytes remaining
	 */
	void write(long position, ByteBuffer source) throws UncheckedIOException;

	/**
	 * Truncates the file to a length of 0.
	 *
	 * @throws UncheckedIOException
	 *             if an {@link IOException} occurs while truncating or if this {@code WritableFile} is already closed
	 */
	void truncate() throws UncheckedIOException;

	/**
	 * <p>
	 * Requests a write of all pending changes to the underlying
	 * file system.
	 * <p>
	 * However, invoking this method can not guarantee that data is actually
	 * written to disk but does only guarantee that data has been handed of to
	 * the operating system.
	 *
	 * @throws UncheckedIOException
	 *             if an {@link IOException} occurs while truncating or if this {@code WritableFile} is already closed
	 */
	void flush() throws UncheckedIOException;

	/**
	 * <p>
	 * Closes this {@code WritableFile} and has the effect of {@link #flush()}.
	 * <p>
	 * After a {@code WritableFile} has been closed all other operations except {{@link #isOpen()} will
	 * throw an {@link UncheckedIOException}.
	 * <p>
	 * Invoking this method on a {@link WritableFile} which has already been
	 * closed does nothing.
	 *
	 * @throws UncheckedIOException
	 *             if an {@link IOException} occurs while closing
	 */
	@Override
	void close() throws UncheckedIOException;

}
