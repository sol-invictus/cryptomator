package org.cryptomator.filesystem.nio;

import static java.lang.String.format;
import static org.cryptomator.filesystem.ReadResult.NO_EOF;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.file.Path;

import org.cryptomator.filesystem.ReadResult;
import org.cryptomator.filesystem.ReadableFile;

class ReadableNioFile implements ReadableFile {

	private final Path path;
	private final SharedFileChannel channel;

	private boolean open = true;

	public ReadableNioFile(Path path, SharedFileChannel channel) {
		this.path = path;
		this.channel = channel;
		channel.openForReading();
	}

	@Override
	public ReadResult read(long offset, ByteBuffer target) throws UncheckedIOException {
		assertOpen();
		if (channel.readFully(offset, target) == SharedFileChannel.EOF) {
			return ReadResult.EOF;
		} else if (target.hasRemaining()) {
			return ReadResult.EOF_REACHED;
		} else {
			return NO_EOF;
		}
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public long size() throws UncheckedIOException {
		return channel.size();
	}

	@Override
	public void close() {
		if (!open) {
			return;
		}
		open = false;
		channel.close();
	}

	private void assertOpen() {
		if (!open) {
			throw new UncheckedIOException(format("%s already closed.", this), new ClosedChannelException());
		}
	}

	@Override
	public String toString() {
		return format("ReadableNioFile(%s)", path);
	}

}