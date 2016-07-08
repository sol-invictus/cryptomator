package org.cryptomator.filesystem.nio;

import static java.lang.String.format;
import static org.cryptomator.filesystem.CreateMode.CREATE_IF_MISSING;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import org.cryptomator.filesystem.FileSystem;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class WritableNioFileTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private FileSystem fileSystem;

	private Path path;

	private SharedFileChannel channel;

	private WritableNioFile inTest;

	@Before
	public void setup() {
		fileSystem = mock(FileSystem.class);
		channel = mock(SharedFileChannel.class);
		path = mock(Path.class);
		inTest = new WritableNioFile(fileSystem, path, channel, CREATE_IF_MISSING);
	}

	public class ConstructorTests {

		@Test
		public void testConstructorSetsFileSystem() {
			assertThat(inTest.fileSystem(), is(fileSystem));
		}

		@Test
		public void testConstructorSetsPath() {
			assertThat(inTest.path(), is(path));
		}

		@Test
		public void testConstructorSetsChannel() {
			assertThat(inTest.channel(), is(channel));
		}

	}

	public class WriteTests {

		private static final long POSITION = 87273;

		@Test
		public void testWriteOpensChannelIfClosedBeforeInvokingWriteFully() {
			ByteBuffer irrelevant = null;

			inTest.write(POSITION, irrelevant);

			InOrder inOrder = inOrder(channel);
			inOrder.verify(channel).openForWriting(CREATE_IF_MISSING);
			inOrder.verify(channel).writeFully(POSITION, irrelevant);
		}

		@Test
		public void testWriteDoesNotModifyBuffer() {
			ByteBuffer buffer = mock(ByteBuffer.class);

			inTest.write(POSITION, buffer);

			verifyZeroInteractions(buffer);
		}

	}

	public class TruncateTests {

		@Test
		public void testTruncateInvokesChannelsOpenWithModeWriteIfInvokedForTheFirstTimeBeforeInvokingTruncate() {
			inTest.truncate();

			InOrder inOrder = inOrder(channel);
			inOrder.verify(channel).openForWriting(CREATE_IF_MISSING);
			inOrder.verify(channel).truncate(0);
		}

		@Test
		public void testTruncateInvokesChannelsTruncateWithZeroAsParameter() {
			inTest.truncate();

			verify(channel).truncate(0);
		}

		@Test
		public void testTruncateWithSizeInvokesChannelsTruncateWithSameSizeAsParameter() {
			int newSize = 5232;
			inTest.truncate(newSize);

			verify(channel).truncate(newSize);
		}

	}

	public class CloseTests {

		@Test
		public void testCloseClosesChannelIfOpened() {
			inTest.truncate();

			inTest.close();

			verify(channel).close();
		}

		@Test
		public void testCloseDoesNothingOnSecondInvocation() {
			inTest.truncate();

			inTest.close();
			inTest.close();

			verify(channel).close();
		}

	}

	public class IsOpenTests {

		@Test
		public void testIsOpenReturnsTrueForNewInstance() {
			assertThat(inTest.isOpen(), is(true));
		}

		@Test
		public void testIsOpenReturnsFalseForClosedInstance() {
			inTest.close();

			assertThat(inTest.isOpen(), is(false));
		}

	}

	public class OperationsFailIfClosedTests {

		@Test
		public void testWriteFailsIfClosed() {
			inTest.close();
			ByteBuffer irrelevant = null;

			thrown.expect(UncheckedIOException.class);
			thrown.expectMessage("already closed");

			inTest.write(0, irrelevant);
		}

		@Test
		public void testTruncateFailsIfClosed() {
			inTest.close();

			thrown.expect(UncheckedIOException.class);
			thrown.expectMessage("already closed");

			inTest.truncate();
		}

	}

	@Test
	public void testToString() {
		assertThat(inTest.toString(), is(format("WritableNioFile(%s)", path)));
	}

}
