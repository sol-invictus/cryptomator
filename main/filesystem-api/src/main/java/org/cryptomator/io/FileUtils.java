package org.cryptomator.io;

import static org.cryptomator.filesystem.ReadResult.EOF;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.cryptomator.common.ModifiableLong;
import org.cryptomator.filesystem.ReadableFile;
import org.cryptomator.filesystem.WritableFile;

public class FileUtils {

	private static final int BUF_SIZE = 8192;

	public static long copy(ReadableFile readable, WritableFile writable) throws UncheckedIOException {
		ModifiableLong total = new ModifiableLong();
		readFully(readable, buffer -> {
			writable.write(total.get(), buffer);
			total.add(buffer.limit());
		});
		return total.get();
	}

	public static void readFully(ReadableFile readable, Consumer<ByteBuffer> bufferConsumer) throws UncheckedIOException {
		ByteBuffer buffer = ByteBuffer.allocate(BUF_SIZE);
		long position = 0;
		while (readable.read(position, buffer) != EOF) {
			buffer.flip();
			position += buffer.limit();
			bufferConsumer.accept(buffer);
			buffer.clear();
		}
	}

	private FileUtils() {
	}

}
