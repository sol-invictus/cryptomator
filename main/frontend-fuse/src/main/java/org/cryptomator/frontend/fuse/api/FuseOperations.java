package org.cryptomator.frontend.fuse.api;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * All fuse operations implemented by a {@link FuseFilesystem}.
 *
 * @author Markus Kreusch
 */
public interface FuseOperations {

	FuseResult access(String path);

	FuseResult create(String path, WritableFileHandle fileHandleConsumer);

	FuseResult fgetattr(String path, Attributes attributes, FileHandle fileHandle);

	FuseResult flush(String path, FileHandle fileHandle);

	FuseResult fsync(String path, boolean flushMetadata, FileHandle fileHandle);

	FuseResult fsyncdir(String path, boolean flushMetadata, FileHandle fileHandle);

	FuseResult ftruncate(String path, long offset, FileHandle fileHandle);

	FuseResult getattr(String path, Attributes attributes);

	FuseResult lock(String path);

	FuseResult mkdir(String path);

	FuseResult open(String path, WritableFileHandle fileHandleConsumer);

	FuseResult opendir(String path, WritableFileHandle fileHandleConsumer);

	FuseResult read(String path, ByteBuffer buffer, long size, long offset, FileHandle fileHandle);

	FuseResult readdir(String path, Consumer<String> filler);

	FuseResult release(String path, FileHandle fileHandle);

	FuseResult releasedir(String path, FileHandle fileHandle);

	FuseResult rename(String path, String newPath);

	FuseResult rmdir(String path);

	FuseResult statfs(String path, FilesystemStats stats);

	FuseResult truncate(String path, long offset);

	FuseResult unlink(String path);

	FuseResult utimens(String path, Times times);

	FuseResult write(String path, ByteBuffer buffer, long bufSize, long writeOffset, FileHandle fileHandle);

}