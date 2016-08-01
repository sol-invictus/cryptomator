package org.cryptomator.frontend.fuse.fusejnaadapter;

import java.nio.ByteBuffer;

import org.slf4j.Logger;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FlockCommand;
import net.fusejna.StructFlock.FlockWrapper;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.StructStatvfs.StatvfsWrapper;
import net.fusejna.StructTimeBuffer.TimeBufferWrapper;
import net.fusejna.XattrFiller;
import net.fusejna.XattrListFiller;
import net.fusejna.types.TypeMode.ModeWrapper;

/**
 * <p>
 * An adapter from a {@link org.cryptomator.frontend.fuse.api.FuseFilesystem cryptomator FuseFileystem} to a {@link net.fusejna.FuseFilesystem fusejna FuseFilesystem}.
 * <p>
 * Delegates all operations and adds gid and uid of the fuse user for {@link #fgetattr(String, StatWrapper, FileInfoWrapper)} and {@link #getattr(String, StatWrapper)}.
 *
 * @author Markus Kreusch
 */
public class FuseFilesystemToFuseJnaAdapter extends net.fusejna.FuseFilesystem {

	private static final long BLOCK_SIZE = 32_000;
	private static final int SUCCESS = 0;
	private static final int NOT_SUPPORTED = -ErrorCodes.ENODEV();

	private final org.cryptomator.frontend.fuse.api.FuseFilesystem delegate;
	private final org.cryptomator.frontend.fuse.api.FuseOperations operations;
	private final Logger fuseLogger;

	public FuseFilesystemToFuseJnaAdapter(org.cryptomator.frontend.fuse.api.FuseFilesystem delegate, Logger fuseLogger) {
		this.delegate = delegate;
		this.operations = delegate.operations();
		this.fuseLogger = fuseLogger;
	}

	@Override
	public int fgetattr(String path, StatWrapper stat, FileInfoWrapper info) {
		int result = operations.fgetattr(path, new StatWrapperToAttributesAdapter(stat), info::fh).getAsInt();
		if (result == SUCCESS && isMounted()) {
			stat.gid(getFuseContextGid().longValue());
			stat.uid(getFuseContextUid().longValue());
		}
		return result;
	}

	@Override
	public int getattr(String path, StatWrapper stat) {
		int result = operations.getattr(path, new StatWrapperToAttributesAdapter(stat)).getAsInt();
		if (result == SUCCESS && isMounted()) {
			stat.gid(getFuseContextGid().longValue());
			stat.uid(getFuseContextUid().longValue());
		}
		return result;
	}

	@Override
	public int truncate(String path, long offset) {
		return operations.truncate(path, offset).getAsInt();
	}

	@Override
	public int create(String path, ModeWrapper mode, FileInfoWrapper info) {
		return operations.create(path, info::fh).getAsInt();
	}

	@Override
	public int open(String path, FileInfoWrapper info) {
		return operations.open(path, info::fh).getAsInt();
	}

	@Override
	public int write(String path, ByteBuffer buffer, long bufSize, long writeOffset, FileInfoWrapper wrapper) {
		return operations.write(path, buffer, bufSize, writeOffset, wrapper::fh).getAsInt();
	}

	@Override
	public int read(String path, ByteBuffer buffer, long size, long offset, FileInfoWrapper info) {
		return operations.read(path, buffer, size, offset, info::fh).getAsInt();
	}

	@Override
	public int release(String path, FileInfoWrapper info) {
		return operations.release(path, info::fh).getAsInt();
	}

	@Override
	public int rename(String path, String newPath) {
		return operations.rename(path, newPath).getAsInt();
	}

	@Override
	public int mkdir(String path, ModeWrapper mode) {
		return operations.mkdir(path).getAsInt();
	}

	@Override
	public int unlink(String path) {
		return operations.unlink(path).getAsInt();
	}

	@Override
	public int readdir(String path, DirectoryFiller filler) {
		return operations.readdir(path, filler::add).getAsInt();
	}

	@Override
	public int flush(String path, FileInfoWrapper info) {
		return operations.flush(path, info::fh).getAsInt();
	}

	@Override
	public int fsync(String path, int datasync, FileInfoWrapper info) {
		return operations.fsync(path, datasync == 0, info::fh).getAsInt();
	}

	@Override
	public int utimens(String path, TimeBufferWrapper wrapper) {
		return operations.utimens(path, new TimeBufferWrapperToTimesAdapter(wrapper)).getAsInt();
	}

	@Override
	public int statfs(String path, StatvfsWrapper wrapper) {
		return operations.statfs(path, new StatvfsWrapperToFilesystemStatsAdapter(wrapper, BLOCK_SIZE)).getAsInt();
	}

	@Override
	public int ftruncate(String path, long offset, FileInfoWrapper info) {
		return operations.ftruncate(path, offset, info::fh).getAsInt();
	}

	@Override
	public int rmdir(String path) {
		return operations.rmdir(path).getAsInt();
	}

	@Override
	public int fsyncdir(String path, int datasync, FileInfoWrapper info) {
		return operations.fsyncdir(path, datasync == 0, info::fh).getAsInt();
	}

	@Override
	public int releasedir(String path, FileInfoWrapper info) {
		return operations.releasedir(path, info::fh).getAsInt();
	}

	@Override
	public int access(String path, int access) {
		return operations.access(path).getAsInt();
	}

	@Override
	public int opendir(String path, FileInfoWrapper info) {
		return operations.opendir(path, info::fh).getAsInt();
	}

	@Override
	public int bmap(String path, FileInfoWrapper info) {
		return notSupported("bmap");
	}

	@Override
	public int chmod(String path, ModeWrapper mode) {
		return notSupported("chmod");
	}

	@Override
	public int chown(String path, long uid, long gid) {
		return notSupported("chown");
	}

	@Override
	public int getxattr(String path, String xattr, XattrFiller filler, long size, long position) {
		return notSupported("getxattr");
	}

	@Override
	public int link(String path, String target) {
		return notSupported("link");
	}

	@Override
	public int listxattr(String path, XattrListFiller filler) {
		return notSupported("listxattr");
	}

	@Override
	public int lock(String path, FileInfoWrapper info, FlockCommand command, FlockWrapper flock) {
		return notSupported("lock");
	}

	@Override
	public int mknod(String path, ModeWrapper mode, long dev) {
		return notSupported("mknod");
	}

	@Override
	public int readlink(String path, ByteBuffer buffer, long size) {
		return notSupported("readlink");
	}

	@Override
	public int removexattr(String path, String xattr) {
		return notSupported("removexattr");
	}

	@Override
	public int setxattr(String path, String xattr, ByteBuffer buf, long size, int flags, int position) {
		return notSupported("setxattr");
	}

	@Override
	public int symlink(String path, String target) {
		return notSupported("symlink");
	}

	private int notSupported(String operationName) {
		fuseLogger.info("Unsupported operation {} invoked", operationName);
		return NOT_SUPPORTED;
	}

	@Override
	protected String getName() {
		return delegate.getName();
	}

	@Override
	protected String[] getOptions() {
		return null;
	}

	@Override
	public void init() {
		delegate.init();
	}

	@Override
	public void afterUnmount(java.io.File mountPoint) {
		delegate.afterUnmount(mountPoint);
	}

	@Override
	public void beforeMount(java.io.File mountPoint) {
		delegate.beforeMount(mountPoint);
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}

}
