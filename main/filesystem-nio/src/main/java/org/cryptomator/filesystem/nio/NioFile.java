package org.cryptomator.filesystem.nio;

import static java.lang.String.format;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import org.cryptomator.filesystem.CreateMode;
import org.cryptomator.filesystem.File;
import org.cryptomator.filesystem.ReadableFile;
import org.cryptomator.filesystem.WritableFile;

class NioFile extends NioNode implements File {

	private final SharedFileChannel sharedChannel;

	public NioFile(Optional<NioFolder> parent, Path eventuallyNonAbsolutePath, NioAccess nioAccess, InstanceFactory instanceFactory) {
		super(parent, eventuallyNonAbsolutePath, nioAccess, instanceFactory);
		sharedChannel = instanceFactory.sharedFileChannel(path, nioAccess);
	}

	@Override
	public ReadableFile openReadable() throws UncheckedIOException {
		return instanceFactory.readableNioFile(path, sharedChannel);
	}

	@Override
	public WritableFile openWritable(CreateMode mode) throws UncheckedIOException {
		return instanceFactory.writableNioFile(fileSystem(), path, sharedChannel, mode);
	}

	@Override
	public boolean exists() throws UncheckedIOException {
		return nioAccess.isRegularFile(path);
	}

	@Override
	public Instant lastModified() throws UncheckedIOException {
		if (nioAccess.exists(path) && !exists()) {
			throw new UncheckedIOException(new IOException(format("%s is a folder", path)));
		}
		return super.lastModified();
	}

	@Override
	public Optional<Instant> creationTime() throws UncheckedIOException {
		if (nioAccess.exists(path) && !exists()) {
			throw new UncheckedIOException(new IOException(format("%s is a folder", path)));
		}
		return super.creationTime();
	}

	@Override
	public void moveTo(File destination) throws UncheckedIOException {
		if (destination == this) {
			return;
		} else if (belongsToSameFilesystem(destination)) {
			internalMoveTo((NioFile) destination);
		} else {
			throw new IllegalArgumentException("Can only move to a File from the same FileSystem");
		}
	}

	private void assertMovePreconditionsAreMet(NioFile destination) {
		if (nioAccess.isDirectory(path())) {
			throw new UncheckedIOException(new IOException(format("Can not move %s to %s. Source is a directory", path(), destination.path())));
		}
		if (nioAccess.isDirectory(destination.path())) {
			throw new UncheckedIOException(new IOException(format("Can not move %s to %s. Target is a directory", path(), destination.path())));
		}
	}

	private void internalMoveTo(NioFile destination) {
		assertMovePreconditionsAreMet(destination);
		try {
			nioAccess.move(path(), destination.path(), REPLACE_EXISTING);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void delete() throws UncheckedIOException {
		if (!exists()) {
			return;
		}
		try {
			nioAccess.delete(path());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public int compareTo(File o) {
		if (belongsToSameFilesystem(o)) {
			return path.compareTo(((NioFile) o).path);
		} else {
			throw new IllegalArgumentException("Can not mix File objects from different file systems");
		}
	}

	@Override
	public String toString() {
		return format("NioFile(%s)", path);
	}

	@Override
	public long size() {
		try {
			return nioAccess.size(path);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
