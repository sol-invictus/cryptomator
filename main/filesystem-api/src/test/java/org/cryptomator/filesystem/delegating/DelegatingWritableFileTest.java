/*******************************************************************************
 * Copyright (c) 2015 Sebastian Stenzel and others.
 * This file is licensed under the terms of the MIT license.
 * See the LICENSE.txt file for more info.
 *
 * Contributors:
 *     Sebastian Stenzel - initial API and implementation
 *******************************************************************************/
package org.cryptomator.filesystem.delegating;

import static org.mockito.Mockito.verify;

import java.nio.ByteBuffer;

import org.cryptomator.filesystem.WritableFile;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class DelegatingWritableFileTest {

	@Test
	public void testIsOpen() {
		WritableFile mockWritableFile = Mockito.mock(WritableFile.class);
		@SuppressWarnings("resource")
		DelegatingWritableFile delegatingWritableFile = new DelegatingWritableFile(mockWritableFile);

		Mockito.when(mockWritableFile.isOpen()).thenReturn(true);
		Assert.assertTrue(delegatingWritableFile.isOpen());

		Mockito.when(mockWritableFile.isOpen()).thenReturn(false);
		Assert.assertFalse(delegatingWritableFile.isOpen());
	}

	@Test
	public void testTruncate() {
		WritableFile mockWritableFile = Mockito.mock(WritableFile.class);
		@SuppressWarnings("resource")
		DelegatingWritableFile delegatingWritableFile = new DelegatingWritableFile(mockWritableFile);

		delegatingWritableFile.truncate();
		Mockito.verify(mockWritableFile).truncate();
	}

	@Test
	public void testWrite() {
		WritableFile mockWritableFile = Mockito.mock(WritableFile.class);
		@SuppressWarnings("resource")
		DelegatingWritableFile delegatingWritableFile = new DelegatingWritableFile(mockWritableFile);

		ByteBuffer buf = ByteBuffer.allocate(4);
		delegatingWritableFile.write(3L, buf);
		verify(mockWritableFile).write(3L, buf);
	}

	@Test
	public void testFlush() {
		WritableFile mockWritableFile = Mockito.mock(WritableFile.class);
		@SuppressWarnings("resource")
		DelegatingWritableFile delegatingWritableFile = new DelegatingWritableFile(mockWritableFile);

		delegatingWritableFile.flush();
		verify(mockWritableFile).flush();
	}

	@Test
	public void testClose() {
		WritableFile mockWritableFile = Mockito.mock(WritableFile.class);
		DelegatingWritableFile delegatingWritableFile = new DelegatingWritableFile(mockWritableFile);

		delegatingWritableFile.close();
		Mockito.verify(mockWritableFile).close();
	}

}
