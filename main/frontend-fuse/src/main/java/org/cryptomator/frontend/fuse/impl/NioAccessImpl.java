package org.cryptomator.frontend.fuse.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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

}
