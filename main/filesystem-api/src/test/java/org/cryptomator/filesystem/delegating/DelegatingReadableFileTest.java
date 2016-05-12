/*******************************************************************************
 * Copyright (c) 2015 Sebastian Stenzel and others.
 * This file is licensed under the terms of the MIT license.
 * See the LICENSE.txt file for more info.
 *
 * Contributors:
 *     Sebastian Stenzel - initial API and implementation
 *******************************************************************************/
package org.cryptomator.filesystem.delegating;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;

import org.cryptomator.filesystem.ReadResult;
import org.cryptomator.filesystem.ReadableFile;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DelegatingReadableFileTest {

	@Test
	public void testIsOpen() {
		ReadableFile mockReadableFile = Mockito.mock(ReadableFile.class);
		@SuppressWarnings("resource")
		DelegatingReadableFile delegatingReadableFile = new DelegatingReadableFile(mockReadableFile);

		Mockito.when(mockReadableFile.isOpen()).thenReturn(true);
		Assert.assertTrue(delegatingReadableFile.isOpen());

		Mockito.when(mockReadableFile.isOpen()).thenReturn(false);
		Assert.assertFalse(delegatingReadableFile.isOpen());
	}

	@Test
	public void testRead() {
		ReadableFile mockReadableFile = Mockito.mock(ReadableFile.class);
		@SuppressWarnings("resource")
		DelegatingReadableFile delegatingReadableFile = new DelegatingReadableFile(mockReadableFile);

		ByteBuffer buf = ByteBuffer.allocate(4);
		when(mockReadableFile.read(1L, buf)).thenReturn(ReadResult.EOF_REACHED);
		assertEquals(ReadResult.EOF_REACHED, delegatingReadableFile.read(1L, buf));
	}

	@Test
	public void testSize() {
		ReadableFile mockReadableFile = Mockito.mock(ReadableFile.class);
		@SuppressWarnings("resource")
		DelegatingReadableFile delegatingReadableFile = new DelegatingReadableFile(mockReadableFile);

		Mockito.when(mockReadableFile.size()).thenReturn(42l);
		Assert.assertEquals(42l, delegatingReadableFile.size());
		Mockito.verify(mockReadableFile).size();
	}

	@Test
	public void testClose() {
		ReadableFile mockReadableFile = Mockito.mock(ReadableFile.class);
		DelegatingReadableFile delegatingReadableFile = new DelegatingReadableFile(mockReadableFile);

		delegatingReadableFile.close();
		Mockito.verify(mockReadableFile).close();
	}

}
