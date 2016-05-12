/*******************************************************************************
 * Copyright (c) 2015 Sebastian Stenzel and others.
 * This file is licensed under the terms of the MIT license.
 * See the LICENSE.txt file for more info.
 *
 * Contributors:
 *     Sebastian Stenzel - initial API and implementation
 *******************************************************************************/
package org.cryptomator.filesystem.delegating;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;

import org.cryptomator.filesystem.WritableFile;

public class DelegatingWritableFile implements WritableFile {

	final WritableFile delegate;

	public DelegatingWritableFile(WritableFile delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean isOpen() {
		return delegate.isOpen();
	}

	@Override
	public void truncate() throws UncheckedIOException {
		delegate.truncate();
	}

	@Override
	public void write(long position, ByteBuffer source) throws UncheckedIOException {
		delegate.write(position, source);
	}

	@Override
	public void close() throws UncheckedIOException {
		delegate.close();
	}

	@Override
	public void flush() throws UncheckedIOException {
		delegate.flush();
	}

}
