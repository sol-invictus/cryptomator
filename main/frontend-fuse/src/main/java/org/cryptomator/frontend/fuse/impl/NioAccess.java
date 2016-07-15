package org.cryptomator.frontend.fuse.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.CopyOption;
import java.nio.file.FileStore;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.stream.Stream;

public interface NioAccess {

	boolean exists(Path path, LinkOption ... options);
	
	Stream<Path> list(Path path) throws IOException;
	
	<A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption ... options) throws IOException;

	void createFile(Path path, FileAttribute<?> ... attrs) throws IOException;
	
	void createDirectory(Path path, FileAttribute<?> ... attrs) throws IOException;

	boolean isRegularFile(Path path, LinkOption ... options);
	
	boolean isDirectory(Path path, LinkOption ... options);
	
	void delete(Path path) throws IOException;

	FileStore getFileStore(Path root) throws IOException;

	void move(Path from, Path to, CopyOption ... options) throws IOException;

	AsynchronousFileChannel openAsyncFileChannel(Path path, OpenOption ... options) throws IOException;
	
}
