package org.cryptomator.filesystem.nio;

import static java.lang.String.format;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.file.Path;

import org.cryptomator.filesystem.CreateMode;
import org.cryptomator.filesystem.FileSystem;
import org.cryptomator.filesystem.WritableFile;

class WritableNioFile implements WritableFile {

	private final FileSystem fileSystem;
	private final Path path;
	private final SharedFileChannel channel;

	private boolean open = true;

	public WritableNioFile(FileSystem fileSystem, Path path, SharedFileChannel channel, CreateMode mode) {
		this.fileSystem = fileSystem;
		this.path = path;
		this.channel = channel;
		channel.openForWriting(mode);
	}

	@Override
	public void write(long offset, ByteBuffer source) throws UncheckedIOException {
		assertOpen();
		channel.writeFully(offset, source);
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public void truncate(long newSize) throws UncheckedIOException {
		assertOpen();
		channel.truncate(newSize);
	}

	@Override
	public void close() throws UncheckedIOException {
		if (!open) {
			return;
		}
		open = false;
		channel.close();
	}

	FileSystem fileSystem() {
		return fileSystem;
	}

	Path path() {
		return path;
	}

	SharedFileChannel channel() {
		return channel;
	}

	void assertOpen() {
		if (!open) {
			throw new UncheckedIOException(format("%s already closed.", this), new ClosedChannelException());
		}
	}

	@Override
	public String toString() {
		return format("WritableNioFile(%s)", path);
	}

	@Override
	public void flush() throws UncheckedIOException {
		channel.flush();
	}

	@Override
	public long size() throws UncheckedIOException {
		return channel.size();
	}

}