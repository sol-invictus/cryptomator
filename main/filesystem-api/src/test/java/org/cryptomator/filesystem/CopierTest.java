package org.cryptomator.filesystem;

import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static org.cryptomator.filesystem.CreateMode.CREATE_IF_MISSING;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class CopierTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	public class CopyFiles {

		private static final int MIBIBYTE = 1024 * 1024;

		@Mock
		private File source;

		@Mock
		private File destination;

		@Mock
		private ReadableFile readable;

		@Mock
		private WritableFile writable;

		@Before
		public void setUp() {
			when(source.openReadable()).thenReturn(readable);
			when(destination.openWritable(CREATE_IF_MISSING)).thenReturn(writable);
		}

		@Test
		public void testCopyFileReadsAndWritesReadableSourceAndWritableDestintationUntilEof() {
			ByteBuffer data = ByteBuffer.allocate(1 * MIBIBYTE);
			ByteBuffer result = ByteBuffer.allocate(1 * MIBIBYTE);
			fillWithRandomData(data);
			when(readable.read(anyLong(), any())).then(readFrom(data));
			doAnswer(writeTo(result)).when(writable).write(anyLong(), any());

			Copier.copy(source, destination);

			InOrder inOrder = inOrder(readable, writable);
			inOrder.verify(writable).truncate();
			inOrder.verify(writable).close();
			inOrder.verify(readable).close();

			result.clear();
			data.clear();
			assertThat(result, is(equalTo(data)));
		}

		private void fillWithRandomData(ByteBuffer data) {
			Random random = new Random();
			byte[] randomBytes = new byte[1024];
			while (data.hasRemaining()) {
				random.nextBytes(randomBytes);
				data.put(randomBytes, 0, min(data.remaining(), randomBytes.length));
			}
		}

		private Answer<ReadResult> readFrom(ByteBuffer data) {
			return new Answer<ReadResult>() {
				@Override
				public ReadResult answer(InvocationOnMock invocation) throws Throwable {
					Long position = invocation.getArgumentAt(0, Long.class);
					ByteBuffer buffer = invocation.getArgumentAt(1, ByteBuffer.class);
					if (position >= data.capacity()) {
						return ReadResult.EOF;
					} else {
						data.position(position.intValue());
						data.limit(min(data.position() + buffer.remaining(), data.capacity()));
						buffer.put(data);
						if (buffer.hasRemaining()) {
							return ReadResult.EOF_REACHED;
						} else {
							return ReadResult.NO_EOF;
						}
					}
				}

			};
		}

		private Answer<Void> writeTo(ByteBuffer result) {
			return new Answer<Void>() {
				@Override
				public Void answer(InvocationOnMock invocation) throws Throwable {
					Long position = invocation.getArgumentAt(0, Long.class);
					ByteBuffer buffer = invocation.getArgumentAt(1, ByteBuffer.class);
					result.position(position.intValue());
					result.put(buffer);
					if (buffer.hasRemaining()) {
						throw new IllegalStateException("Attempt to write more bytes than read");
					}
					return null;
				}

			};
		}

	}

	public class CopyFolders {

		@Mock
		private Folder source;

		@Mock
		private Folder destination;

		@Test
		public void testCopyFolderDeletesAndCreatesDestinationBeforeIteratingOverTheFilesAndFoldersInSource() {
			when(source.files()).thenReturn(Stream.empty());
			when(source.folders()).thenReturn(Stream.empty());

			Copier.copy(source, destination);

			InOrder inOrder = inOrder(source, destination);
			inOrder.verify(destination).delete();
			inOrder.verify(destination).create();
			inOrder.verify(source).files();
			inOrder.verify(source).folders();
		}

		@Test
		@SuppressWarnings({"unchecked", "rawtypes"})
		public void testCopyFolderInvokesCopyToOnAllFilesInSourceWithFileWithSameNameFromDestination() {
			String filename1 = "nameOfFile1";
			String filename2 = "nameOfFile2";
			File file1 = mock(File.class);
			File file2 = mock(File.class);
			File destinationFile1 = mock(File.class);
			File destinationFile2 = mock(File.class);
			when(source.files()).thenReturn((Stream) asList(file1, file2).stream());
			when(source.folders()).thenReturn(Stream.empty());
			when(destination.file(filename1)).thenReturn(destinationFile1);
			when(destination.file(filename2)).thenReturn(destinationFile2);
			when(file1.name()).thenReturn(filename1);
			when(file2.name()).thenReturn(filename2);

			Copier.copy(source, destination);

			verify(file1).copyTo(destinationFile1);
			verify(file2).copyTo(destinationFile2);
		}

		@Test
		@SuppressWarnings({"unchecked", "rawtypes"})
		public void testCopyFolderInvokesCopyToOnAllFoldersInSourceWithFolderWithSameNameFromDestination() {
			String folderName1 = "nameOfFolder1";
			String folderName2 = "nameOfFolder2";
			Folder folder1 = mock(Folder.class);
			Folder folder2 = mock(Folder.class);
			Folder destinationfolder1 = mock(Folder.class);
			Folder destinationfolder2 = mock(Folder.class);
			when(source.folders()).thenReturn((Stream) asList(folder1, folder2).stream());
			when(source.files()).thenReturn(Stream.empty());
			when(destination.folder(folderName1)).thenReturn(destinationfolder1);
			when(destination.folder(folderName2)).thenReturn(destinationfolder2);
			when(folder1.name()).thenReturn(folderName1);
			when(folder2.name()).thenReturn(folderName2);

			Copier.copy(source, destination);

			verify(folder1).copyTo(destinationfolder1);
			verify(folder2).copyTo(destinationfolder2);
		}

		@Test
		public void testCopyFolderFailsWithIllegalArgumentExceptionIfSourceIsNestedInDestination() {
			when(source.isAncestorOf(destination)).thenReturn(false);
			when(destination.isAncestorOf(source)).thenReturn(true);

			thrown.expect(IllegalArgumentException.class);

			Copier.copy(source, destination);
		}

		@Test
		public void testCopyFolderFailsWithIllegalArgumentExceptionIfDestinationIsNestedInSource() {
			when(source.isAncestorOf(destination)).thenReturn(true);
			when(destination.isAncestorOf(source)).thenReturn(false);

			thrown.expect(IllegalArgumentException.class);

			Copier.copy(source, destination);
		}

	}

}
