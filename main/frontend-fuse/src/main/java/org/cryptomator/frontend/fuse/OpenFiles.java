package org.cryptomator.frontend.fuse;

import java.util.HashMap;
import java.util.Map;

import org.cryptomator.filesystem.FileSystem;

import net.fusejna.StructFuseFileInfo.FileInfoWrapper.OpenMode;

public class OpenFiles {

	private final FileSystem fileSystem;
	private final Map<Long,OpenFile> filesByHandle = new HashMap<>();
	
	public OpenFiles(FileSystem fileSystem) {
		this.fileSystem = fileSystem;
	}

	public OpenFile open(String path, OpenMode mode) throws OpenException {
		OpenFile openFile = OpenFile.open(path).from(fileSystem).afterClose(this::close).inMode(mode);
		filesByHandle.put(openFile.handle(), openFile);
		return openFile;
	}
	
	private void close(OpenFile file) {
		filesByHandle.remove(file.handle());
	}

	public OpenFile get(long handle) {
		return filesByHandle.get(handle);
	}
	
}
