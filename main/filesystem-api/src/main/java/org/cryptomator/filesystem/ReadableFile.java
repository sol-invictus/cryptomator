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
 * A {@code ReadableFile} allows to read a {@link File}.
 * <p>
 * Instances can safely be used by multiple threads.
 */
public interface ReadableFile extends AutoCloseable {

	/**
	 * @return {@code true} if this ReadableFile is still open. Otherwise
	 *         {@code false}.
	 */
	boolean isOpen();

	/**
	 * Reads data from this {@code ReadableFile} into the given {@link ByteBuffer} starting at
	 * the given position.
	 * <p>
	 * This methods tries to fill the given buffer completely. Thus the buffer will always have {@link ByteBuffer#remaining()}
	 * of zero if the end of file has not or exactly been reached while reading.
	 *
	 * @param position
	 *            the position to start reading
	 * @param source
	 *            the byte buffer to fill
	 * @throws UncheckedIOException
	 *             if an {@link IOException} occurs while writing or if this {@code ReadableFile} is already closed
	 * @return a {@link ReadResult} telling if EOF has been reached immediately, while filling the buffer or has not been reached.
	 */
	ReadResult read(long position, ByteBuffer target) throws UncheckedIOException;

	/**
	 * @return The current size of the file.
	 * @throws UncheckedIOException
	 *             if an {@link IOException} occurs or if this {@code ReadableFile} is already closed
	 */
	long size() throws UncheckedIOException;

	/**
	 * <p>
	 * Closes this {@code ReadableFile}.
	 * <p>
	 * After a {@code ReadableFile} has been closed all other operations except {@link #isOpen()} will
	 * throw an {@link UncheckedIOException}.
	 * <p>
	 * Invoking this method on a {@link ReadableFile} which has already been
	 * closed does nothing.
	 *
	 * @throws UncheckedIOException
	 *             if an {@link IOException} occurs while closing
	 */
	@Override
	void close() throws UncheckedIOException;

}
