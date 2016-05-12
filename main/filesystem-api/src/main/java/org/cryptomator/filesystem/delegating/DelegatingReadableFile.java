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

import org.cryptomator.filesystem.ReadResult;
import org.cryptomator.filesystem.ReadableFile;

public class DelegatingReadableFile implements ReadableFile {

	private final ReadableFile delegate;

	public DelegatingReadableFile(ReadableFile delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean isOpen() {
		return delegate.isOpen();
	}

	@Override
	public ReadResult read(long position, ByteBuffer target) throws UncheckedIOException {
		return delegate.read(position, target);
	}

	@Override
	public long size() throws UncheckedIOException {
		return delegate.size();
	}

	@Override
	public void close() throws UncheckedIOException {
		delegate.close();
	}

}
