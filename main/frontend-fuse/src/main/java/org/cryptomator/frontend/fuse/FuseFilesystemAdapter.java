package org.cryptomator.frontend.fuse;

import static java.lang.Math.min;
import static java.time.Instant.EPOCH;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

import org.cryptomator.filesystem.File;
import org.cryptomator.filesystem.FileSystem;
import org.cryptomator.filesystem.Folder;
import org.cryptomator.filesystem.Node;
import org.cryptomator.filesystem.ReadableFile;
import org.cryptomator.filesystem.WritableFile;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.FlockCommand;
import net.fusejna.FuseFilesystem;
import net.fusejna.StructFlock.FlockWrapper;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.StructStatvfs.StatvfsWrapper;
import net.fusejna.StructTimeBuffer.TimeBufferWrapper;
import net.fusejna.XattrFiller;
import net.fusejna.XattrListFiller;
import net.fusejna.types.TypeMode.ModeWrapper;
import net.fusejna.types.TypeMode.NodeType;
import net.fusejna.util.FuseFilesystemAdapterFull;

/**
 * TODO:
 * <ul>
 * 	<li>Wrap in another {@link FuseFilesystem} which returns EIO on all and logs Exceptions
 * </ul>
 */
public class FuseFilesystemAdapter extends FuseFilesystemAdapterFull {

	private static final int SUCCESS = 0;
	
	private final FileSystem delegate;
	
	public FuseFilesystemAdapter(FileSystem delegate) {
		this.delegate = delegate;
	} 
	
	@Override
	public int getattr(final String path, final StatWrapper stat) {
		return resolve(path,
				file -> getattr(file, stat),
				folder -> getattr(folder, stat),
				() -> -ErrorCodes.ENOENT());
	}
	
	private int getattr(File file, StatWrapper stat) {
		try (ReadableFile readableFile = file.openReadable()) {
			stat.setMode(NodeType.FILE) //
				.size(readableFile.size())
				.mtime(file.lastModified().getEpochSecond())
				.ctime(file.creationTime().orElse(EPOCH).getEpochSecond());
			if (isMounted()) {
				stat
					.uid(getFuseContextUid().longValue())
					.gid(getFuseContextGid().longValue());
			}
		}
		return SUCCESS;
	}
	
	private int getattr(Folder folder, StatWrapper stat) {
		stat.setMode(NodeType.DIRECTORY)
			.mtime(folder.lastModified().getEpochSecond())
			.ctime(folder.creationTime().orElse(EPOCH).getEpochSecond());
		if (isMounted()) {
			stat
				.uid(getFuseContextUid().longValue())
				.gid(getFuseContextGid().longValue());
		}
		return SUCCESS;
	}
	
	@Override
	public int create(String path, ModeWrapper mode, FileInfoWrapper info) {
		return resolve(path,
				file -> -ErrorCodes.EEXIST(),
				folder -> -ErrorCodes.EEXIST(),
				() -> createNonExisting(path, mode, info));
	}
	
	private int createNonExisting(String path, ModeWrapper mode, FileInfoWrapper info) {
		NodeType type = NodeType.fromBits(mode.mode());
		if (type != null && type != NodeType.FILE) {
			return -ErrorCodes.ENOSYS();
		}
		File file = delegate.resolveFile(path);
		if (!file.parent().get().exists()) {
			return -ErrorCodes.ENOENT();
		}
		try (WritableFile writable = file.openWritable()) {
			writable.write(ByteBuffer.allocate(0));
		}
		return SUCCESS;
	}

	@Override
	public int write(String path, ByteBuffer buf, long bufSize, long writeOffset, FileInfoWrapper wrapper) {
		File file = delegate.resolveFile(path); 
		if (file.exists()) {
			try (WritableFile writable = file.openWritable()) {
				writable.position(writeOffset);
				buf.limit((int)Math.min(buf.limit(), buf.position() + bufSize));
				return writable.write(buf);
			}
		}
		return -ErrorCodes.ENOENT();
	}

	@Override
	public int truncate(String path, long offset) {
		File file = delegate.resolveFile(path); 
		if (file.exists()) {
			try (WritableFile writable = file.openWritable()) {
				writable.truncate();
			}
			return 0;
		}
		return -ErrorCodes.ENOENT();
	}

	@Override
	public int read(final String path, final ByteBuffer buffer, final long size, final long offset, final FileInfoWrapper info) {
		File file = delegate.resolveFile(path); 
		if (file.exists()) {
			try (ReadableFile readable = file.openReadable()) {
				readable.position(offset);
				buffer.limit((int)min(buffer.limit(), buffer.position() + size));
				int read = readable.read(buffer);
				if (read == -1) {
					return 0;
				} else {
					return read;
				}
			}
		}
		return -ErrorCodes.ENOENT();
	}
	
	@Override
	public int rename(String path, String newName) {
		Folder targetFolder = delegate.resolveFolder(newName);
		File targetFile = delegate.resolveFile(newName);
		Folder targetParent = targetFolder.parent().get();
		if (targetFolder.exists() || targetFile.exists()) {
			return -ErrorCodes.EEXIST();
		}
		if (!targetParent.exists()) {
			return -ErrorCodes.ENOENT();
		}
		if (targetParent.parent().isPresent() && targetParent.parent().get().file(targetParent.name()).exists()) {
			return -ErrorCodes.ENOTDIR();
		}
		File file = delegate.resolveFile(path);
		if (file.exists()) {
			file.moveTo(targetFile);
			return 0;
		}
		Folder folder = delegate.resolveFolder(path);
		if (folder.exists()) {
			folder.moveTo(targetFolder);
			return 0;
		}
		return -ErrorCodes.ENOENT();
	}
	
	@Override
	public int mkdir(String path, ModeWrapper mode) {
		if (delegate.resolveFile(path).exists()) {
			return -ErrorCodes.EEXIST();
		}
		Folder folder = delegate.resolveFolder(path);
		if (folder.exists()) {
			return -ErrorCodes.EEXIST();
		}
		if (!folder.parent().get().exists()) {
			return -ErrorCodes.ENOENT();
		}
		folder.create();
		return 0;
	}
	
	@Override
	public int unlink(String path) {
		File file = delegate.resolveFile(path);
		if (file.exists()) {
			file.delete();
			return 0;
		}
		Folder folder = delegate.resolveFolder(path);
		if (folder.exists()) {
			folder.delete();
			return 0;
		}
		return -ErrorCodes.ENOENT();
	}

	@Override
	public int readdir(final String path, final DirectoryFiller filler)
	{
		Folder folder = delegate.resolveFolder(path);
		if (folder.exists()) {
			folder.children().map(Node::name).forEach(filler::add);
			return 0;
		}
		return -ErrorCodes.ENOENT();
	}

	@Override
	public int access(String path, int access) {
		return 0;
	}

	@Override
	public int bmap(String path, FileInfoWrapper info) {
		return -ErrorCodes.ENODEV();
	}

	@Override
	public int chmod(String path, ModeWrapper mode) {
		return -ErrorCodes.ENODEV();
	}

	@Override
	public int chown(String path, long uid, long gid) {
		return -ErrorCodes.ENODEV(); 
	}

	@Override
	public int fgetattr(String path, StatWrapper stat, FileInfoWrapper info) {
		return resolve(path,
				file -> getattr(file, stat),
				folder -> getattr(folder, stat),
				() -> -ErrorCodes.ENOENT());
	}

	@Override
	public int flush(String path, FileInfoWrapper info) {
		return 0;
	}

	@Override
	public int fsync(String path, int datasync, FileInfoWrapper info) {
		return 0;
	}

	@Override
	public int fsyncdir(String path, int datasync, FileInfoWrapper info) {
		return 0;
	}

	@Override
	public int getxattr(String path, String xattr, XattrFiller filler, long size, long position) {
		return -ErrorCodes.ENODEV();
	}

	@Override
	public int link(String path, String target) {
		return -ErrorCodes.ENODEV();
	}

	@Override
	public int listxattr(String path, XattrListFiller filler) {
		return -ErrorCodes.ENODEV(); 
	}

	@Override
	public int lock(String path, FileInfoWrapper info, FlockCommand command, FlockWrapper flock) {
		return -ErrorCodes.ENODEV();
	}

	@Override
	public int mknod(String path, ModeWrapper mode, long dev) {
		return -ErrorCodes.ENOSYS();
	}

	@Override
	public int open(String path, FileInfoWrapper info) {
		return resolve(path,
				file -> 0,
				folder -> -ErrorCodes.ENOENT(),
				() -> -ErrorCodes.ENOENT());
	}

	@Override
	public int opendir(String path, FileInfoWrapper info) {
		return resolve(path,
				file -> -ErrorCodes.ENOENT(),
				folder -> 0,
				() -> -ErrorCodes.ENOENT());
	}

	@Override
	public int readlink(String path, ByteBuffer buffer, long size) {
		return -ErrorCodes.ENODEV();
	}

	@Override
	public int release(String path, FileInfoWrapper info) {
		return 0;
	}

	@Override
	public int releasedir(String path, FileInfoWrapper info) {
		return 0;
	}

	@Override
	public int removexattr(String path, String xattr) {
		return -ErrorCodes.ENODEV();
	}

	@Override
	public int rmdir(String path) {
		Folder folder = delegate.resolveFolder(path);
		if (folder.exists()) {
			if (folder.children().count() > 0) {
				return -ErrorCodes.ENOTEMPTY();
			}
			folder.delete();
			return 0;
		}
		return -ErrorCodes.ENOENT();
	}

	@Override
	public int setxattr(String path, String xattr, ByteBuffer buf, long size, int flags, int position) {
		return -ErrorCodes.ENODEV();
	}

	@Override
	public int symlink(String path, String target) {
		return -ErrorCodes.ENODEV();
	}

	@Override
	public int utimens(String path, TimeBufferWrapper wrapper) {
		// TODO atime
		return resolve(path,
				file -> {
					file.setLastModified(Instant.ofEpochSecond(wrapper.mod_sec(), wrapper.mod_nsec()));
					return 0;
				},
				folder -> {
					folder.setLastModified(Instant.ofEpochSecond(wrapper.mod_sec(), wrapper.mod_nsec()));
					return 0;
				},
				() -> -ErrorCodes.ENOENT());
	}

	@Override
	public int statfs(String path, StatvfsWrapper wrapper) {
		// TODO blocksize/count wtf?
		/** Get file system statistics
		 *
		 * The 'f_frsize', 'f_favail', 'f_fsid' and 'f_flag' fields are ignored
		 *
		 * Replaced 'struct statfs' parameter with 'struct statvfs' in
		 * version 2.5
		 */
		wrapper.setBlockInfo(500000, 500000, 1000000);
		wrapper.setSizes(4096, 4096);
		return super.statfs(path, wrapper);
	}
	
	private int resolve(final String path, ToIntFunction<File> onFile, ToIntFunction<Folder> onFolder, IntSupplier onNothing) {
		if (!path.endsWith("/")) {
			File file = delegate.resolveFile(path);
			if (file.exists()) {
				return onFile.applyAsInt(file);
			}
		}
		Folder folder = delegate.resolveFolder(path);
		if (folder.exists()) {
			return onFolder.applyAsInt(folder);
		}
		return onNothing.getAsInt();
	}

}
