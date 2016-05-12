package org.cryptomator.io;

import static org.cryptomator.io.FileUtils.readFully;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.cryptomator.filesystem.File;
import org.cryptomator.filesystem.ReadableFile;
import org.cryptomator.filesystem.WritableFile;

public final class FileContents {

	public static final FileContents UTF_8 = FileContents.withCharset(StandardCharsets.UTF_8);
	private static final long MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	private final Charset charset;

	private FileContents(Charset charset) {
		this.charset = charset;
	}

	/**
	 * Reads the whole content from the given file.
	 *
	 * @param file File whose content should be read.
	 * @return The file's content interpreted in this FileContents' charset.
	 */
	public String readContents(File file) {
		try (ReadableFile readable = file.openReadable()) {
			long size = readable.size();
			if (size > MAX_ARRAY_SIZE) {
				throw new UncheckedIOException(new IOException("File to large to read into String"));
			}
			ByteArrayOutputStream result = new ByteArrayOutputStream((int) size);
			readFully(readable, buffer -> {
				byte[] bufferAsArray = new byte[buffer.remaining()];
				buffer.get(bufferAsArray);
				result.write(bufferAsArray, 0, bufferAsArray.length);
			});
			return new String(result.toByteArray(), charset);
		}
	}

	/**
	 * Writes the string into the file encoded with this FileContents' charset.
	 * This methods replaces any previously existing content, i.e. the string will be the sole content.
	 *
	 * @param file File whose content should be written.
	 * @param content The new content.
	 */
	public void writeContents(File file, String content) {
		try (WritableFile writable = file.openWritable()) {
			writable.truncate();
			writable.write(0L, charset.encode(content));
		}
	}

	public static FileContents withCharset(Charset charset) {
		return new FileContents(charset);
	}

}
