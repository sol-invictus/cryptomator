package org.cryptomator.filesystem.nio;

import static java.lang.String.format;
import static org.cryptomator.filesystem.CreateMode.CREATE_AND_FAIL_IF_PRESENT;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

public class DefaultInstanceFactoryTest {

	private NioFileSystem fileSystem;
	private Optional<NioFolder> parent;
	private Path path;
	private NioAccess nioAccess;
	private SharedFileChannel channel;

	private final DefaultInstanceFactory inTest = new DefaultInstanceFactory();

	@Before
	public void setUp() {
		fileSystem = mock(NioFileSystem.class);
		parent = Optional.of(fileSystem);
		path = mock(Path.class);
		channel = mock(SharedFileChannel.class);
		when(path.toAbsolutePath()).thenReturn(path);
		nioAccess = mock(NioAccess.class);
	}

	@Test
	public void testNioFolderCreatesNioFolder() {
		NioFolder result = inTest.nioFolder(parent, path, nioAccess);

		assertThat(result.parent(), is(parent));
		assertThat(result.path(), is(path));

		result.exists();
		verify(nioAccess).isDirectory(path);
	}

	@Test
	public void testNioFileCreatesNioFile() {
		NioFile result = inTest.nioFile(parent, path, nioAccess);

		assertThat(result.parent(), is(parent));

		result.exists();
		verify(nioAccess).isRegularFile(path);
	}

	@Test
	public void testSharedFileChannelCreatesSharedFileChannel() throws IOException {
		when(nioAccess.isRegularFile(path)).thenReturn(true);
		SharedFileChannel result = inTest.sharedFileChannel(path, nioAccess);

		result.openForReading();
		verify(nioAccess).open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
	}

	@Test
	public void testWritableNioFileCreatesWritableNioFile() throws IOException {
		WritableNioFile result = inTest.writableNioFile(fileSystem, path, channel, CREATE_AND_FAIL_IF_PRESENT);

		assertThat(result.path(), is(path));
		assertThat(result.channel(), is(channel));
		assertThat(result.fileSystem(), is(fileSystem));

		verify(channel).openForWriting(CREATE_AND_FAIL_IF_PRESENT);
		result.close();
		verify(channel).close();
	}

	@Test
	public void testReadableNioFileCreatesWritableNioFile() throws IOException {
		ReadableNioFile result = inTest.readableNioFile(path, channel);

		assertThat(result.toString(), is(format("ReadableNioFile(%s)", path)));

		result.close();
		verify(channel).close();
	}

}
