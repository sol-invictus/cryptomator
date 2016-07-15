package org.cryptomator.frontend.fuse.impl;

import static org.cryptomator.frontend.fuse.api.StandardFuseResult.INVALID_FILE_HANDLE;
import static org.cryptomator.frontend.fuse.api.StandardFuseResult.SUCCESS;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.cryptomator.frontend.fuse.api.FileHandle;
import org.cryptomator.frontend.fuse.api.FuseResult;
import org.cryptomator.frontend.fuse.api.WritableFileHandle;

@Singleton
class OpenFiles {

	private final AtomicLong nextHandle = new AtomicLong(1);
	private final ConcurrentMap<Long,OpenFile> openFiles = new ConcurrentHashMap<>();
	private final OpenFileFactory openFileFactory;
	
	@Inject
	public OpenFiles(OpenFileFactory openFileFactory) {
		this.openFileFactory = openFileFactory;
	}
	
	public FuseResult open(Path path, WritableFileHandle fileHandleConsumer) {
		long handle = nextHandle.getAndIncrement();
		openFiles.put(handle, open(path));
		fileHandleConsumer.accept(handle);
		return SUCCESS;
	}
	
	public OpenFile open(Path path) {
		return openFileFactory.newOpenFile(path);
	}

	public FuseResult release(Path path, FileHandle fileHandle) {
		Optional<OpenFile> openFile = get(path, fileHandle);
		if (openFile.isPresent()) {
			openFiles.remove(fileHandle.getAsLong());
			openFile.get().release();
			return SUCCESS;
		} else {
			return INVALID_FILE_HANDLE;
		}
	}
	
	public Optional<OpenFile> get(Path path, FileHandle fileHandle) {
		OpenFile openFile = openFiles.get(fileHandle.getAsLong());
		if (openFile == null || !openFile.hasPath(path)) {
			return Optional.empty();
		} else {
			return Optional.of(openFile);
		}
	}

}
