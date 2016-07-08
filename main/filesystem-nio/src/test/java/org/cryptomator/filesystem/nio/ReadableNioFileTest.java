package org.cryptomator.filesystem.nio;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import org.cryptomator.filesystem.ReadResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class ReadableNioFileTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private Path path;

	private SharedFileChannel channel;

	private ReadableNioFile inTest;

	@Before
	public void setup() {
		path = mock(Path.class);
		channel = mock(SharedFileChannel.class);
		inTest = new ReadableNioFile(path, channel);
	}

	@Test
	public void testConstructorInvokesOpenForReading() {
		verify(channel).openForReading();
	}

	@Test
	public void testReadFailsIfClosed() {
		ByteBuffer irrelevant = null;
		inTest.close();

		thrown.expect(UncheckedIOException.class);
		thrown.expectMessage("already closed");

		inTest.read(0, irrelevant);
	}

	@Test
	public void testSizeReturnsSizeOfChannel() {
		long expectedSize = 85472;
		when(channel.size()).thenReturn(expectedSize);

		long actualSize = inTest.size();

		assertThat(actualSize, is(expectedSize));
	}

	@Test
	public void testReadDelegatesToChannelReadFullyWithPassedPosition() {
		long position = 578372;
		ByteBuffer buffer = mock(ByteBuffer.class);

		inTest.read(position, buffer);

		verify(channel).readFully(position, buffer);
	}

	@Test
	public void testReadReturnsEofIfReadFullyDid() {
		ByteBuffer buffer = mock(ByteBuffer.class);
		long position = 578372;
		when(channel.readFully(position, buffer)).thenReturn(SharedFileChannel.EOF);

		ReadResult result = inTest.read(position, buffer);

		assertThat(result, is(ReadResult.EOF));
	}

	@Test
	public void testReadReturnsEofReachedIfBufferHasRemaining() {
		ByteBuffer buffer = ByteBuffer.allocate(1);
		long position = 578372;

		ReadResult result = inTest.read(position, buffer);

		assertThat(result, is(ReadResult.EOF_REACHED));
	}

	@Test
	public void testReadReturnsNoEofIfBufferWasFilled() {
		ByteBuffer buffer = ByteBuffer.allocate(0);
		long position = 578372;

		ReadResult result = inTest.read(position, buffer);

		assertThat(result, is(ReadResult.NO_EOF));
	}

	@Test
	public void testReadDoesNotModifyBuffer() {
		ByteBuffer buffer = mock(ByteBuffer.class);

		inTest.read(12371, buffer);

		verifyZeroInteractions(buffer);
	}

	@Test
	public void testIsOpenReturnsTrueForNewReadableNioFile() {
		assertThat(inTest.isOpen(), is(true));
	}

	@Test
	public void testIsOpenReturnsFalseForClosed() {
		inTest.close();

		assertThat(inTest.isOpen(), is(false));
	}

	@Test
	public void testCloseClosesChannel() {
		inTest.close();

		verify(channel).close();
	}

	@Test
	public void testCloseClosesChannelOnlyOnceIfInvokedTwice() {
		inTest.close();
		inTest.close();

		verify(channel).close();
	}

	@Test
	public void testToString() {
		assertThat(inTest.toString(), is(format("ReadableNioFile(%s)", path)));
	}

}
