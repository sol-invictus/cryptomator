package org.cryptomator.frontend.fuse.impl;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Arrays.stream;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.cryptomator.frontend.fuse.api.Attributes;
import org.cryptomator.frontend.fuse.api.FileHandle;
import org.cryptomator.frontend.fuse.api.FilesystemStats;
import org.cryptomator.frontend.fuse.api.FuseOperations;
import org.cryptomator.frontend.fuse.api.FuseResult;
import org.cryptomator.frontend.fuse.api.StandardFuseResult;
import org.cryptomator.frontend.fuse.api.Times;
import org.cryptomator.frontend.fuse.api.WritableFileHandle;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class NioFuseOperationsTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private Path root = mock(Path.class);
	
	private OpenFiles openFiles = mock(OpenFiles.class);
	
	private GetattrCache getattrCache = mock(GetattrCache.class);
	
	private NioAccess nioAccess = mock(NioAccess.class);
	
	private FuseOperations inTest;
	
	@Before
	public void setUp() {
		GetattrCacheFactory getattrCacheFactory = mock(GetattrCacheFactory.class);
		when(getattrCacheFactory.create(any())).thenReturn(getattrCache);
		
		when(nioAccess.exists(root)).thenReturn(true);
		when(nioAccess.isDirectory(root)).thenReturn(true);
		
		inTest = new NioFuseOperations(root, nioAccess, openFiles, getattrCacheFactory);
	}

	@After
	public void tearDown() {
		validateMockitoUsage();
	}
	
	public class Access {
	
		@Test
		public void testAccessReturnsSuccessIfFileExists() {
			MockedPath existingPath = mockExistingPath();
			
			FuseResult result = inTest.access(existingPath.asString());
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
		}
		
		@Test
		public void testAccessReturnsFileDoesNotExistIfFileDoesNotExist() {
			MockedPath nonExistingPath = mockNonExistingPath();
			
			FuseResult result = inTest.access(nonExistingPath.asString());
			
			assertThat(result, is(StandardFuseResult.FILE_DOES_NOT_EXIST));
		}
		
		@Test
		public void testRootExists() {
			FuseResult result = inTest.access("/");
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
			verify(nioAccess).exists(root);
		}
		
	}
	
	public class Create {

		@Test
		public void testCreateDoesNotCreateFileIfItAlreadyExists() throws IOException {
			MockedPath existingPath = mockExistingPath();
			
			FuseResult result = inTest.create(existingPath.asString());
			
			assertThat(result, is(StandardFuseResult.FILE_EXISTS));
			verify(nioAccess, never()).createFile(any());
		}
		
		@Test
		public void testCreateCreatesFileIfItDoesNotExist() throws IOException {
			MockedPath nonExistingPath = mockNonExistingPath();
			
			FuseResult result = inTest.create(nonExistingPath.asString());
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
			verify(nioAccess).createFile(nonExistingPath.resolvedAgainstRoot());
		}
		
		@Test
		public void testCreateWrapsIOException() throws IOException {
			String pathAsString = UUID.randomUUID().toString();
			Path path = mock(Path.class);
			when(root.resolve(pathAsString)).thenReturn(path);
			IOException exception = new IOException();
			doThrow(exception).when(nioAccess).createFile(path);
			
			thrown.expect(UncheckedIOException.class);
			thrown.expectCause(is(exception));
			
			inTest.create(pathAsString);
		}
		
	}
	
	public class GetattrAndFgetattr {
		
		@Test
		public void testGetattrUsesAttributesFromCache() throws IOException {
			MockedPath existingPath = mockExistingPath();
			Attributes attributes = mock(Attributes.class);
			GetattrResult getattrResult = mock(GetattrResult.class);
			when(getattrCache.get(existingPath.resolvedAgainstRoot())).thenReturn(getattrResult);
			
			FuseResult result = inTest.getattr(existingPath.asString(), attributes);
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
			verify(getattrResult).fill(attributes);
		}
		
		@Test
		public void testFgetattrReturnsInvalidFileHandleForNonOpenFile() throws IOException {
			Attributes attributes = mock(Attributes.class);
			MockedFile nonOpenFile = mockNonOpenFile();
			
			FuseResult result = inTest.fgetattr(nonOpenFile.path().asString(), attributes, nonOpenFile.handle());
			
			assertThat(result, is(StandardFuseResult.INVALID_FILE_HANDLE));
		}

		@Test
		public void testGetattrReloadsAttributesUsingCache() throws IOException {
			Attributes attributes = mock(Attributes.class);
			GetattrResult getattrResult = mock(GetattrResult.class);
			MockedFile openFile = mockOpenFile();
			when(getattrCache.reload(openFile.path().resolvedAgainstRoot())).thenReturn(getattrResult);
			
			FuseResult result = inTest.fgetattr(openFile.path().asString(), attributes, openFile.handle());
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
			verify(getattrResult).fill(attributes);
		}
		
	}
	
	public class GetattrLoader {
		
		private Function<Path,GetattrResult> loader;
		
		@Before
		public void setUp() {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			ArgumentCaptor<Function<Path,GetattrResult>> captor = (ArgumentCaptor)ArgumentCaptor.forClass(Function.class);
			GetattrCacheFactory getattrCacheFactory = mock(GetattrCacheFactory.class);
			when(getattrCacheFactory.create(captor.capture())).thenReturn(getattrCache);
			
			new NioFuseOperations(root, nioAccess, openFiles, getattrCacheFactory);
			
			loader = captor.getValue();
		}
		
		@Test
		public void testLoaderReturnsCorrectValuesForFile() throws IOException {
			long creation = 12372;
			long access = 43722;
			long modified = 84721;
			long size = 3472;
			Path path = mock(Path.class);
			BasicFileAttributes basicFileAttributes = mock(BasicFileAttributes.class);
			when(basicFileAttributes.creationTime()).thenReturn(FileTime.fromMillis(creation));
			when(basicFileAttributes.lastAccessTime()).thenReturn(FileTime.fromMillis(access));
			when(basicFileAttributes.lastModifiedTime()).thenReturn(FileTime.fromMillis(modified));
			when(basicFileAttributes.size()).thenReturn(size);
			when(basicFileAttributes.isRegularFile()).thenReturn(true);
			Attributes attributes = mock(Attributes.class);
			when(nioAccess.readAttributes(path, BasicFileAttributes.class)).thenReturn(basicFileAttributes);
			
			loader.apply(path).fill(attributes);
			
			verify(attributes).accessTime(access);
			verify(attributes).creationTime(creation);
			verify(attributes).modificationTime(modified);
			verify(attributes).size(size);
			verify(attributes).file();
		}
		
		@Test
		public void testLoaderReturnsCorrectValuesForDirectory() throws IOException {
			long creation = 12372;
			long access = 43722;
			long modified = 84721;
			Path path = mock(Path.class);
			BasicFileAttributes basicFileAttributes = mock(BasicFileAttributes.class);
			when(basicFileAttributes.creationTime()).thenReturn(FileTime.fromMillis(creation));
			when(basicFileAttributes.lastAccessTime()).thenReturn(FileTime.fromMillis(access));
			when(basicFileAttributes.lastModifiedTime()).thenReturn(FileTime.fromMillis(modified));
			when(basicFileAttributes.isDirectory()).thenReturn(true);
			Attributes attributes = mock(Attributes.class);
			when(nioAccess.readAttributes(path, BasicFileAttributes.class)).thenReturn(basicFileAttributes);
			
			loader.apply(path).fill(attributes);
			
			verify(attributes).accessTime(access);
			verify(attributes).creationTime(creation);
			verify(attributes).modificationTime(modified);
			verify(attributes).folder();
		}
		
		@Test
		public void testLoaderWrapsIoExceptions() throws IOException {
			Path path = mock(Path.class);
			IOException exception = new IOException();
			when(nioAccess.readAttributes(path, BasicFileAttributes.class)).thenThrow(exception);
			
			thrown.expect(UncheckedIOException.class);
			thrown.expectCause(is(exception));
			
			loader.apply(path);
		}
		
	}
	
	public class Flush {
	
		@Test
		public void testFlushDelegatesToOpenFile() {
			MockedFile openFile = mockOpenFile();
			FuseResult expectedResult = mock(FuseResult.class);
			when(openFile.openFile().flush()).thenReturn(expectedResult);
			
			FuseResult result = inTest.flush(openFile.path().asString(), openFile.handle());
			
			assertThat(result, is(expectedResult));
		}
		
		@Test
		public void testFlushReturnsInvalidFileHandleForNonOpenFile() {
			MockedFile nonOpenFile = mockNonOpenFile();
			
			FuseResult result = inTest.flush(nonOpenFile.path().asString(), nonOpenFile.handle());
			
			assertThat(result, is(StandardFuseResult.INVALID_FILE_HANDLE));
		}
		
	}
	
	public class Fsync {
	
		@Test
		public void testFsyncDelegatesToOpenFile() {
			MockedFile openFile = mockOpenFile();
			FuseResult expectedResult = mock(FuseResult.class);
			when(openFile.openFile().fsync(true)).thenReturn(expectedResult);
			
			FuseResult result = inTest.fsync(openFile.path().asString(), true, openFile.handle());
			
			assertThat(result, is(expectedResult));
		}
		
		@Test
		public void testFsyncReturnsInvalidFileHandleForNonOpenFile() {
			MockedFile nonOpenFile = mockNonOpenFile();
			
			FuseResult result = inTest.fsync(nonOpenFile.path().asString(), true, nonOpenFile.handle());
			
			assertThat(result, is(StandardFuseResult.INVALID_FILE_HANDLE));
		}
		
	}
	
	public class Fsyncdir {
		
		@Test
		public void testFsyncdirReturnsSuccessIfFileExists() {
			MockedPath existingPath = mockExistingPath();
			
			FuseResult result = inTest.fsyncdir(existingPath.asString(), true, () -> -1);
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
		}
		
		@Test
		public void testFsyncdirReturnsFileDoesNotExistIfFileDoesNotExist() {
			MockedPath nonExistingPath = mockNonExistingPath();
			
			FuseResult result = inTest.fsyncdir(nonExistingPath.asString(), true, () -> -1);
			
			assertThat(result, is(StandardFuseResult.FILE_DOES_NOT_EXIST));
		}
		
	}
	
	public class TruncateAndFtruncate {
		
		@Test
		public void testTruncateFailsForNonExistingFile() {
			MockedPath nonExistingPath = mockNonExistingPath();
			long offset = 3727;
			
			FuseResult result = inTest.truncate(nonExistingPath.asString(), offset);
			
			assertThat(result, is(StandardFuseResult.FILE_DOES_NOT_EXIST));
		}
		
		@Test
		public void testTruncateDelegatesToOpenFileForExistingFile() {
			MockedPath existingPath = mockExistingPath();
			long offset = 3727;
			OpenFile openFile = mock(OpenFile.class);
			when(openFiles.open(existingPath.resolvedAgainstRoot())).thenReturn(openFile);
			FuseResult expectedResult = mock(FuseResult.class);
			when(openFile.truncate(offset)).thenReturn(expectedResult);
			
			FuseResult result = inTest.truncate(existingPath.asString(), offset);
			
			assertThat(result, is(expectedResult));
			InOrder inOrder = inOrder(openFile);
			inOrder.verify(openFile).truncate(offset);
			inOrder.verify(openFile).release();
			inOrder.verifyNoMoreInteractions();
		}
		
		@Test
		public void testOpenFileIsReleasedEvenIfExceptionOccursDuringTruncate() {
			MockedPath existingPath = mockExistingPath();
			long offset = 3727;
			OpenFile openFile = mock(OpenFile.class);
			RuntimeException expectedException = new RuntimeException();
			when(openFiles.open(existingPath.resolvedAgainstRoot())).thenReturn(openFile);
			when(openFile.truncate(offset)).thenThrow(expectedException);
			
			RuntimeException exception = null;
			try {
				inTest.truncate(existingPath.asString(), offset);
			} catch (RuntimeException e) {
				exception = e;
			}
			
			assertThat(exception, is(expectedException));
			InOrder inOrder = inOrder(openFile);
			inOrder.verify(openFile).truncate(offset);
			inOrder.verify(openFile).release();
			inOrder.verifyNoMoreInteractions();
		}
	
		@Test
		public void testFtruncateDelegatesToOpenFile() {
			long offset = 3727;
			MockedFile openFile = mockOpenFile();
			FuseResult expectedResult = mock(FuseResult.class);
			when(openFile.openFile().truncate(offset)).thenReturn(expectedResult);
			
			FuseResult result = inTest.ftruncate(openFile.path().asString(), offset, openFile.handle());
			
			assertThat(result, is(expectedResult));
		}
		
		@Test
		public void testFsyncReturnsInvalidFileHandleForNonOpenFile() {
			MockedFile nonOpenFile = mockNonOpenFile();
			
			FuseResult result = inTest.ftruncate(nonOpenFile.path().asString(), 232, nonOpenFile.handle());
			
			assertThat(result, is(StandardFuseResult.INVALID_FILE_HANDLE));
		}
		
	}
	
	@Test
	public void testLockReturnsUnsupportedOperation() {
		String irrelevant = null;
		
		assertThat(inTest.lock(irrelevant), is(StandardFuseResult.UNSUPPORTED_OPERATION));
	}
	
	public class Mkdir {

		@Test
		public void testMkdirDoesNotCreateDirectoryIfItAlreadyExists() throws IOException {
			MockedPath existingPath = mockExistingPath();
			
			FuseResult result = inTest.mkdir(existingPath.asString());
			
			assertThat(result, is(StandardFuseResult.FILE_EXISTS));
			verify(nioAccess, never()).createDirectory(any());
		}
		
		@Test
		public void testMkdirCreatesDirectoryIfItDoesNotExist() throws IOException {
			MockedPath nonExistingPath = mockNonExistingPath();
			
			FuseResult result = inTest.mkdir(nonExistingPath.asString());
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
			verify(nioAccess).createDirectory(nonExistingPath.resolvedAgainstRoot());
		}
		
	}
	
	public class OpenAndRelease {
		
		@Test
		public void testOpenDelegatesToOpenFiles() {
			MockedPath path = mockExistingPath();
			WritableFileHandle handle = mock(WritableFileHandle.class);
			FuseResult expectedResult = mock(FuseResult.class);
			when(openFiles.open(path.resolvedAgainstRoot(), handle)).thenReturn(expectedResult);
			
			FuseResult result = inTest.open(path.asString(), handle);
			
			assertThat(result, is(expectedResult));
		}
		
		@Test
		public void testReleaseDelegatesToOpenFiles() {
			MockedPath path = mockExistingPath();
			FileHandle handle = mock(FileHandle.class);
			FuseResult expectedResult = mock(FuseResult.class);
			when(openFiles.release(path.resolvedAgainstRoot(), handle)).thenReturn(expectedResult);
			
			FuseResult result = inTest.release(path.asString(), handle);
			
			assertThat(result, is(expectedResult));
		}
		
	}
	
	public class OpendirAndReleasedir {
		
		@Test
		public void testOpendirSucceedsWithExistingDirectory() {
			MockedPath existingPath = mockExistingPath();
			
			FuseResult result = inTest.opendir(existingPath.asString(), handle -> {});
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
		}
		
		@Test
		public void testReleasedirSucceedsWithExistingDirectory() {
			MockedPath existingPath = mockExistingPath();
			
			FuseResult result = inTest.releasedir(existingPath.asString(), () -> -1);
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
		}
		
		@Test
		public void testOpendirFailsWithNonExistingDirectory() {
			MockedPath nonExistingPath = mockNonExistingPath();
			
			FuseResult result = inTest.opendir(nonExistingPath.asString(), handle -> {});
			
			assertThat(result, is(StandardFuseResult.FILE_DOES_NOT_EXIST));
		}
		
		@Test
		public void testReleasedirFailsWithNonExistingDirectory() {
			MockedPath nonExistingPath = mockNonExistingPath();
			
			FuseResult result = inTest.releasedir(nonExistingPath.asString(), () -> -1);
			
			assertThat(result, is(StandardFuseResult.FILE_DOES_NOT_EXIST));
		}
		
	}
	
	public class Read {
	
		@Test
		public void testReadDelegatesToOpenFile() {
			ByteBuffer buffer = ByteBuffer.allocate(10);
			long size = 372;
			FuseResult expectedResult = FuseResult.withValue(48373);
			long offset = 34787;
			MockedFile openFile = mockOpenFile();
			when(openFile.openFile().read(buffer, size, offset)).thenReturn(expectedResult);
			
			FuseResult result = inTest.read(openFile.path().asString(), buffer, size, offset, openFile.handle());
			
			assertThat(result, is(expectedResult));
		}
		
		@Test
		public void testReadFailsForInvalidFileHandle() {
			MockedFile nonOpenFile = mockNonOpenFile();
			
			FuseResult result = inTest.read(nonOpenFile.path().asString(), ByteBuffer.allocate(1), 1, 0, nonOpenFile.handle());
			
			assertThat(result, is(StandardFuseResult.INVALID_FILE_HANDLE));
		}
		
	}
	
	public class Readdir {
	
		@Test
		public void testReaddirReadsFolderContents() throws IOException {
			String[] entries = {"entry1","entry2","entry3"};
			MockedPath existingPath = mockExistingPath();
			@SuppressWarnings("unchecked")
			Consumer<String> filler = mock(Consumer.class);
			when(nioAccess.list(existingPath.resolvedAgainstRoot())).thenReturn(pathStreamOf(entries));
			
			FuseResult result = inTest.readdir(existingPath.asString(), filler);
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
			verify(filler).accept(entries[0]);
			verify(filler).accept(entries[1]);
			verify(filler).accept(entries[2]);
			verifyNoMoreInteractions(filler);
		}
		
		private Stream<Path> pathStreamOf(String[] entries) {
			return stream(entries).map(entry -> {
				Path path = mock(Path.class);
				Path filename = mock(Path.class);
				when(path.getFileName()).thenReturn(filename);
				when(filename.toString()).thenReturn(entry);
				return path;
			});
		}
	
		@Test
		public void testReaddirReturnsFileDoesNotExistForNonExistingFolder() {
			MockedPath nonExistingPath = mockNonExistingPath();
			@SuppressWarnings("unchecked")
			Consumer<String> filler = mock(Consumer.class);
			
			FuseResult result = inTest.readdir(nonExistingPath.asString(), filler);
			
			assertThat(result, is(StandardFuseResult.FILE_DOES_NOT_EXIST));
			verifyZeroInteractions(filler);
		}
		
	}
	
	public class Rename {
		
		@Test
		public void testRenameMovesFileToNonExistingFile() throws IOException {
			MockedPath existingFile = mockExistingFile();
			MockedPath nonExistingFile = mockNonExistingPath();
			
			FuseResult result = inTest.rename(existingFile.asString(), nonExistingFile.asString());
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
			verify(nioAccess).move(existingFile.resolvedAgainstRoot(), nonExistingFile.resolvedAgainstRoot(), REPLACE_EXISTING, COPY_ATTRIBUTES);
		}
		
		@Test
		public void testRenameMovesFileToExistingFile() throws IOException {
			MockedPath existingFile = mockExistingFile();
			MockedPath otherExistingFile = mockExistingFile();
			
			FuseResult result = inTest.rename(existingFile.asString(), otherExistingFile.asString());
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
			verify(nioAccess).move(existingFile.resolvedAgainstRoot(), otherExistingFile.resolvedAgainstRoot(), REPLACE_EXISTING, COPY_ATTRIBUTES);
		}
		
		@Test
		public void testRenameMovesDirectoryToNonExistingDirectory() throws IOException {
			MockedPath existingDirectory = mockExistingDirectory();
			MockedPath nonExistingDirectory = mockNonExistingPath();
			
			FuseResult result = inTest.rename(existingDirectory.asString(), nonExistingDirectory.asString());
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
			verify(nioAccess).move(existingDirectory.resolvedAgainstRoot(), nonExistingDirectory.resolvedAgainstRoot(), REPLACE_EXISTING, COPY_ATTRIBUTES);
		}
		
		@Test
		public void testRenameMovesDirectoryToEmptyExistingDirectory() throws IOException {
			MockedPath existingDirectory = mockExistingDirectory();
			MockedPath otherExistingDirectory = mockExistingDirectory();
			when(nioAccess.list(otherExistingDirectory.resolvedAgainstRoot())).thenReturn(Stream.empty());
			
			FuseResult result = inTest.rename(existingDirectory.asString(), otherExistingDirectory.asString());
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
			verify(nioAccess).move(existingDirectory.resolvedAgainstRoot(), otherExistingDirectory.resolvedAgainstRoot(), REPLACE_EXISTING, COPY_ATTRIBUTES);
		}
		
		@Test
		public void testRenameDoesNotMoveDirectoryToNonEmptyExistingDirectory() throws IOException {
			MockedPath existingDirectory = mockExistingDirectory();
			MockedPath otherExistingDirectory = mockExistingDirectory();
			when(nioAccess.list(otherExistingDirectory.resolvedAgainstRoot())).thenReturn(Stream.of((Path)null));
			
			FuseResult result = inTest.rename(existingDirectory.asString(), otherExistingDirectory.asString());
			
			assertThat(result, is(StandardFuseResult.DIRECTORY_NOT_EMPTY));
		}
		
		@Test
		public void testRenameDoesNotMoveDirectoryToExistingFile() throws IOException {
			MockedPath existingDirectory = mockExistingDirectory();
			MockedPath existingFile = mockExistingFile();
			
			FuseResult result = inTest.rename(existingDirectory.asString(), existingFile.asString());
			
			assertThat(result, is(StandardFuseResult.IS_DIRECTORY));
		}
		
		@Test
		public void testRenameDoesNotMoveFileToExistingDirectory() throws IOException {
			MockedPath existingDirectory = mockExistingDirectory();
			MockedPath existingFile = mockExistingFile();
			
			FuseResult result = inTest.rename(existingFile.asString(), existingDirectory.asString());
			
			assertThat(result, is(StandardFuseResult.IS_NO_DIRECTORY));
		}
		
		@Test
		public void testRenameDoesNotMoveIfSourceDoesNotExist() throws IOException {
			MockedPath nonExistingDirectory = mockNonExistingPath();
			MockedPath existingDirectory = mockExistingDirectory();
			
			FuseResult result = inTest.rename(nonExistingDirectory.asString(), existingDirectory.asString());
			
			assertThat(result, is(StandardFuseResult.FILE_DOES_NOT_EXIST));
		}
		
		@Test
		public void testRenameDoesNotMoveIfParentOfTargetDoesNotExist() throws IOException {
			MockedPath existingDirectory = mockExistingFile();
			MockedPath nonExistingParent = mockNonExistingPath();
			MockedPath pathWithNonExistingParent = mockNonExistingPathWithParent(nonExistingParent.resolvedAgainstRoot());
			
			FuseResult result = inTest.rename(existingDirectory.asString(), pathWithNonExistingParent.asString());
			
			assertThat(result, is(StandardFuseResult.FILE_DOES_NOT_EXIST));
		}

		@Test
		public void testRenameDoesNotMoveDirectoryInsideItself() throws IOException {
			MockedPath existingDirectory = mockExistingDirectory();
			MockedPath subDirectory = mockNonExistingPath();
			when(subDirectory.resolvedAgainstRoot().startsWith(existingDirectory.resolvedAgainstRoot())).thenReturn(true);
			
			FuseResult result = inTest.rename(existingDirectory.asString(), subDirectory.asString());
			
			assertThat(result, is(StandardFuseResult.ILLEGAL_ARGUMENTS));
		}
		
	}
	
	public class Rmdir {
		
		@Test
		public void testRmdirFailsForFile() {
			MockedPath existingFile = mockExistingFile();
			
			FuseResult result = inTest.rmdir(existingFile.asString());
			
			assertThat(result, is(StandardFuseResult.IS_NO_DIRECTORY));
		}
		
		@Test
		public void testRmdirFailsForNonExistingDirectory() {
			MockedPath nonExistingFile = mockNonExistingPath();
			
			FuseResult result = inTest.rmdir(nonExistingFile.asString());
			
			assertThat(result, is(StandardFuseResult.FILE_DOES_NOT_EXIST));
		}
		
		@Test
		public void testRmdirFailsForNonEmptyDirectory() throws IOException {
			MockedPath existingDirectory = mockExistingDirectory();
			Path irrelevant = null;
			when(nioAccess.list(existingDirectory.resolvedAgainstRoot())).thenReturn(Stream.of(irrelevant));
			
			FuseResult result = inTest.rmdir(existingDirectory.asString());
			
			assertThat(result, is(StandardFuseResult.DIRECTORY_NOT_EMPTY));
		}
		
		@Test
		public void testRmdirSucceedsForEmptyDirectory() throws IOException {
			MockedPath existingDirectory = mockExistingDirectory();
			when(nioAccess.list(existingDirectory.resolvedAgainstRoot())).thenReturn(Stream.empty());
			
			FuseResult result = inTest.rmdir(existingDirectory.asString());
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
			verify(nioAccess).delete(existingDirectory.resolvedAgainstRoot());
		}
		
	}
	
	public class Statfs {
		
		@Test
		public void testStatfsReportsCorrectSize() throws IOException {
			String irrelevant = null;
			long usedBytes = 18728;
			long availableBytes = 47882;
			FileStore fileStore = mock(FileStore.class);
			when(nioAccess.getFileStore(root)).thenReturn(fileStore);
			FilesystemStats stats = mock(FilesystemStats.class);
			when(fileStore.getTotalSpace()).thenReturn(usedBytes+availableBytes);
			when(fileStore.getUnallocatedSpace()).thenReturn(availableBytes);
			
			FuseResult result = inTest.statfs(irrelevant, stats);
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
			verify(stats).available(availableBytes);
			verify(stats).used(usedBytes);
		}
		
	}
	
	public class Unlink {
		
		@Test
		public void testUnlinkFailsForDirectory() {
			MockedPath existingDirectory = mockExistingDirectory();
			
			FuseResult result = inTest.unlink(existingDirectory.asString());
			
			assertThat(result, is(StandardFuseResult.IS_DIRECTORY));
		}
		
		@Test
		public void testUnlinkFailsForNonExistingFile() {
			MockedPath nonExistingFile = mockNonExistingPath();
			
			FuseResult result = inTest.unlink(nonExistingFile.asString());
			
			assertThat(result, is(StandardFuseResult.FILE_DOES_NOT_EXIST));
			
		}
		
		@Test
		public void testUnlinkSucceedsForFile() throws IOException {
			MockedPath existingFile = mockExistingFile();
			
			FuseResult result = inTest.unlink(existingFile.asString());
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
			verify(nioAccess).delete(existingFile.resolvedAgainstRoot());
		}
		
	}
	
	public class Utimens {
		
		@Test
		public void testUtimensFailsForNonExistingFile() {
			MockedPath nonExistingPath = mockNonExistingPath();
			Times times = mock(Times.class);
			
			FuseResult result = inTest.utimens(nonExistingPath.asString(), times);
			
			assertThat(result, is(StandardFuseResult.FILE_DOES_NOT_EXIST));
		}
		
		@Test
		public void testUnlinkSetsFileTimesOfExistingEntry() throws IOException {
			MockedPath existingPath = mockExistingPath();
			Times times = mock(Times.class);
			BasicFileAttributes fileAttributes = mock(BasicFileAttributes.class);
			when(nioAccess.readAttributes(existingPath.resolvedAgainstRoot(), BasicFileAttributes.class)).thenReturn(fileAttributes);
			long expectedLastAccessTime = 37271;
			long expectedLastModifiedTime = 16312;
			when(fileAttributes.lastAccessTime()).thenReturn(FileTime.fromMillis(expectedLastAccessTime));
			when(fileAttributes.lastModifiedTime()).thenReturn(FileTime.fromMillis(expectedLastModifiedTime));
			
			FuseResult result = inTest.utimens(existingPath.asString(), times);
			
			assertThat(result, is(StandardFuseResult.SUCCESS));
			verify(times).accessTime(expectedLastAccessTime);
			verify(times).modificationTime(expectedLastModifiedTime);
		}
		
	}
	
	public class Write {
	
		@Test
		public void testWriteDelegatesToOpenFile() {
			ByteBuffer buffer = ByteBuffer.allocate(10);
			long size = 372;
			FuseResult expectedResult = FuseResult.withValue(48373);
			long offset = 34787;
			MockedFile openFile = mockOpenFile();
			when(openFile.openFile().write(buffer, size, offset)).thenReturn(expectedResult);
			
			FuseResult result = inTest.write(openFile.path().asString(), buffer, size, offset, openFile.handle());
			
			assertThat(result, is(expectedResult));
		}
		
		@Test
		public void testWriteFailsForInvalidFileHandle() {
			MockedFile nonOpenFile = mockNonOpenFile();
			
			FuseResult result = inTest.write(nonOpenFile.path().asString(), ByteBuffer.allocate(1), 1, 0, nonOpenFile.handle());
			
			assertThat(result, is(StandardFuseResult.INVALID_FILE_HANDLE));
		}
		
	}
	
	public MockedPath mockExistingFile() {
		String name = UUID.randomUUID().toString();
		Path path = mock(Path.class);
		when(path.getParent()).thenReturn(root);
		when(root.resolve(name)).thenReturn(path);
		when(nioAccess.exists(path)).thenReturn(true);
		when(nioAccess.isRegularFile(path)).thenReturn(true);
		return new MockedPath(name, path);
	}
	
	public MockedPath mockExistingDirectory() {
		String name = UUID.randomUUID().toString();
		Path path = mock(Path.class);
		when(path.getParent()).thenReturn(root);
		when(root.resolve(name)).thenReturn(path);
		when(nioAccess.exists(path)).thenReturn(true);
		when(nioAccess.isDirectory(path)).thenReturn(true);
		return new MockedPath(name, path);
	}
	
	public MockedPath mockExistingPath() {
		String name = UUID.randomUUID().toString();
		Path path = mock(Path.class);
		when(root.resolve(name)).thenReturn(path);
		when(nioAccess.exists(path)).thenReturn(true);
		return new MockedPath(name, path);
	}
	
	public MockedPath mockNonExistingPath() {
		return mockNonExistingPathWithParent(root);
	}
	
	public MockedPath mockNonExistingPathWithParent(Path parent) {
		String name = UUID.randomUUID().toString();
		Path path = mock(Path.class);
		when(path.getParent()).thenReturn(parent);
		when(root.resolve(name)).thenReturn(path);
		when(nioAccess.exists(path)).thenReturn(false);
		return new MockedPath(name, path);
	}
	
	private static final AtomicLong NEXT_HANDLE = new AtomicLong(1);
	
	private MockedFile mockOpenFile() {
		MockedPath path = mockExistingPath();
		long handleValue = NEXT_HANDLE.getAndIncrement();
		FileHandle handle = () -> handleValue;
		OpenFile openFile = mock(OpenFile.class);
		MockedFile result = new MockedFile(path, openFile, handle);
		when(openFile.path()).thenReturn(path.resolvedAgainstRoot());
		when(openFiles.get(path.resolvedAgainstRoot(), handle)).thenReturn(Optional.of(openFile));
		return result;
	}
	
	private MockedFile mockNonOpenFile() {
		MockedPath path = mockExistingPath();
		long handleValue = NEXT_HANDLE.getAndIncrement();
		FileHandle handle = () -> handleValue;
		MockedFile result = new MockedFile(path, null, handle);
		when(openFiles.get(path.resolvedAgainstRoot(), handle)).thenReturn(Optional.empty());
		return result;
	}
	
	private static class MockedFile {
		
		private final MockedPath path;
		private final OpenFile openFile;
		private final FileHandle handle;
		
		private MockedFile(MockedPath path, OpenFile openFile, FileHandle handle) {
			this.path = path;
			this.openFile = openFile;
			this.handle = handle;
		}
		
		public MockedPath path() {
			return path;
		}
		
		public OpenFile openFile() {
			if (openFile == null) {
				throw new IllegalStateException("Mocked file is not open");
			}
			return openFile;
		}
		
		public FileHandle handle() {
			return handle;
		}
		
	}
	
	private static class MockedPath {
		
		private final String name;
		private final Path path;
		
		private MockedPath(String name, Path path) {
			this.name = name;
			this.path = path;
		}
		
		public String asString() {
			return name;
		}
		
		public Path resolvedAgainstRoot() {
			return path;
		}
		
	}
	
}
