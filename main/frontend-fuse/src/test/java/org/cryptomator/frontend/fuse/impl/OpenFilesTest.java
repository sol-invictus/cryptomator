package org.cryptomator.frontend.fuse.impl;

import static org.cryptomator.common.test.matcher.OptionalMatcher.emptyOptional;
import static org.cryptomator.common.test.matcher.OptionalMatcher.presentOptionalWithValueThat;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.INVALID_FILE_HANDLE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Optional;

import org.cryptomator.frontend.fuse.api.FileHandle;
import org.cryptomator.frontend.fuse.api.FuseResult;
import org.cryptomator.frontend.fuse.api.StandardFuseResult;
import org.cryptomator.frontend.fuse.api.WritableFileHandle;
import org.junit.After;
import org.junit.Test;

public class OpenFilesTest {

	private Path aPath = mock(Path.class);
	
	private Path anotherPath = mock(Path.class);
	
	private OpenFile anOpenFile = mock(OpenFile.class);
	
	private OpenFileFactory openFileFactory = mock(OpenFileFactory.class);
	
	private OpenFiles inTest = new OpenFiles(openFileFactory);
	
	@After
	public void tearDown() {
		validateMockitoUsage();
	}
	
	@Test
	public void testOpenWithPathInvokesOpenFileFactory() {
		when(openFileFactory.newOpenFile(aPath)).thenReturn(anOpenFile);
		
		OpenFile result = inTest.open(aPath);
		
		assertThat(result, is(anOpenFile));
	}
	
	@Test
	public void testOpenWithPathAndHandleInvokesOpenFileFactoryAndAssignsUniqueHandle() {
		ReadWriteFileHandle handle1 = new ReadWriteFileHandle();
		ReadWriteFileHandle handle2 = new ReadWriteFileHandle();
		when(openFileFactory.newOpenFile(aPath)).thenReturn(anOpenFile);
		
		FuseResult result = inTest.open(aPath, handle1);
		FuseResult result2 = inTest.open(aPath, handle2);
		
		assertThat(result, is(StandardFuseResult.SUCCESS));
		assertThat(result2, is(StandardFuseResult.SUCCESS));
		assertThat(handle1.handle, is(not(handle2.handle)));
	}
	
	@Test
	public void testGetWithUndefinedHandleReturnsEmptyOptional() {
		Optional<OpenFile> result = inTest.get(aPath, () -> 1);
		
		assertThat(result, is(emptyOptional()));
	}
	
	@Test
	public void testGetWithNonMatchingPathReturnsEmptyOptional() {
		ReadWriteFileHandle handle = new ReadWriteFileHandle();
		when(openFileFactory.newOpenFile(aPath)).thenReturn(anOpenFile);
		when(anOpenFile.hasPath(aPath)).thenReturn(true);
		inTest.open(aPath, handle);
		
		Optional<OpenFile> result = inTest.get(anotherPath, handle);
		
		assertThat(result, is(emptyOptional()));
	}
	
	@Test
	public void testGetWithNonMatchingHandleReturnsEmptyOptional() {
		ReadWriteFileHandle handle = new ReadWriteFileHandle();
		when(openFileFactory.newOpenFile(aPath)).thenReturn(anOpenFile);
		when(anOpenFile.hasPath(aPath)).thenReturn(true);
		inTest.open(aPath, handle);
		
		Optional<OpenFile> result = inTest.get(aPath, () -> handle.getAsLong() + 1);
		
		assertThat(result, is(emptyOptional()));
	}
	
	@Test
	public void testGetWithMatchingHandleAndPathReturnsOptionalOfOpenFile() {
		ReadWriteFileHandle handle = new ReadWriteFileHandle();
		when(openFileFactory.newOpenFile(aPath)).thenReturn(anOpenFile);
		when(anOpenFile.hasPath(aPath)).thenReturn(true);
		inTest.open(aPath, handle);
		
		Optional<OpenFile> result = inTest.get(aPath, handle);
		
		assertThat(result, is(presentOptionalWithValueThat(is(anOpenFile))));
	}
	
	@Test
	public void testReleaseWithUndefinedHandleFails() {
		FuseResult result = inTest.release(aPath, () -> 1);
		
		assertThat(result, is(StandardFuseResult.INVALID_FILE_HANDLE));
	}
	
	@Test
	public void testReleaseWithNonMatchingPathFails() {
		ReadWriteFileHandle handle = new ReadWriteFileHandle();
		when(openFileFactory.newOpenFile(aPath)).thenReturn(anOpenFile);
		when(anOpenFile.hasPath(aPath)).thenReturn(true);
		inTest.open(aPath, handle);
		
		FuseResult result = inTest.release(anotherPath, handle);
		
		assertThat(result, is(StandardFuseResult.INVALID_FILE_HANDLE));
	}
	
	@Test
	public void testReleaseWithNonMatchingHandleFails() {
		ReadWriteFileHandle handle = new ReadWriteFileHandle();
		when(openFileFactory.newOpenFile(aPath)).thenReturn(anOpenFile);
		when(anOpenFile.hasPath(aPath)).thenReturn(true);
		inTest.open(aPath, handle);
		
		FuseResult result = inTest.release(aPath, () -> handle.getAsLong() + 1);
		
		assertThat(result, is(StandardFuseResult.INVALID_FILE_HANDLE));
	}
	
	@Test
	public void testReleaseWithMatchingHandleAndPathSucceeds() {
		ReadWriteFileHandle handle = new ReadWriteFileHandle();
		when(openFileFactory.newOpenFile(aPath)).thenReturn(anOpenFile);
		when(anOpenFile.hasPath(aPath)).thenReturn(true);
		inTest.open(aPath, handle);
		
		FuseResult result = inTest.release(aPath, handle);
		
		assertThat(result, is(StandardFuseResult.SUCCESS));
	}
	
	@Test
	public void testReleaseWithMatchingHandleAndPathMakesHandleInvalid() {
		ReadWriteFileHandle handle = new ReadWriteFileHandle();
		when(openFileFactory.newOpenFile(aPath)).thenReturn(anOpenFile);
		when(anOpenFile.hasPath(aPath)).thenReturn(true);
		inTest.open(aPath, handle);
		
		inTest.release(aPath, handle);
		
		assertThat(inTest.get(aPath, handle), is(emptyOptional()));
		assertThat(inTest.release(aPath, handle), is(INVALID_FILE_HANDLE));
	}
	
	private static class ReadWriteFileHandle implements FileHandle,WritableFileHandle {

		private long handle = -1;
		
		@Override
		public long getAsLong() {
			return handle;
		}

		@Override
		public void accept(long value) {
			handle = value;
		}
		
	}
	
}
