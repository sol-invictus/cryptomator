package org.cryptomator.frontend.fuse.impl;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.CopyOption;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.stream.Stream;

class NioAccessImpl implements NioAccess {

	@Override
	public boolean exists(Path path, LinkOption ... options) {
		return Files.exists(path, options);
	}

	@Override
	public Stream<Path> list(Path path) throws IOException {
		return Files.list(path);
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption ... options) throws IOException {
		return Files.readAttributes(path, type, options);
	}

	@Override
	public void createFile(Path path, FileAttribute<?>... attrs) throws IOException {
		Files.createFile(path, attrs);
	}

	@Override
	public void createDirectory(Path path, FileAttribute<?>... attrs) throws IOException {
		Files.createDirectory(path, attrs);
	}

	@Override
	public boolean isRegularFile(Path path, LinkOption ... options) {
		return Files.isRegularFile(path, options);
	}

	@Override
	public boolean isDirectory(Path path, LinkOption ... options) {
		return Files.isDirectory(path, options);
	}

	@Override
	public void delete(Path path) throws IOException {
		Files.delete(path);
	}

	@Override
	public FileStore getFileStore(Path path) throws IOException {
		return Files.getFileStore(path);
	}

	@Override
	public void move(Path from, Path to, CopyOption ... options) throws IOException {
		Files.move(from, to, options);
	}

	@Override
	public AsynchronousFileChannel openAsyncFileChannel(Path path, OpenOption... options) throws IOException {
		return AsynchronousFileChannel.open(path, options);
	}

}
