package org.cryptomator.frontend.fuse.impl;

import static org.cryptomator.common.test.matcher.PrivateFieldMatcher.hasField;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpenFileFactoryTest {

	private Path path = mock(Path.class);
	private AsynchronousFileChannel channel = mock(AsynchronousFileChannel.class);
	
	private OpenFileFactory inTest;
	
	@Before
	public void setUp() throws IOException {
		NioAccess nioAccess = mock(NioAccess.class);
		when(nioAccess.openAsyncFileChannel(path, StandardOpenOption.READ, StandardOpenOption.WRITE)).thenReturn(channel);
		
		inTest = new OpenFileFactory(nioAccess);
	}
	
	@After
	public void tearDown() {
		validateMockitoUsage();
	}
	
	@Test
	public void testOpenFileFactoryCreatesOpenFiles() {
		
		
		OpenFile result = inTest.newOpenFile(path);
		
		assertThat(result, is(notNullValue()));
		assertThat(result, hasField("path", Path.class).that(is(path)));
		assertThat(result, hasField("channel", AsynchronousFileChannel.class).that(is(channel)));
	}
	
}
