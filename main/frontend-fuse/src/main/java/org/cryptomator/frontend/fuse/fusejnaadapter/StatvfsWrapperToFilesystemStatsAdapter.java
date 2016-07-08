package org.cryptomator.frontend.fuse.fusejnaadapter;

import org.cryptomator.frontend.fuse.api.FilesystemStats;

import net.fusejna.StructStatvfs.StatvfsWrapper;

class StatvfsWrapperToFilesystemStatsAdapter implements FilesystemStats {

	private final long blockSize;
	private final StatvfsWrapper wrapper;

	private long availableBytes = -1;
	private long usedBytes = -1;

	public StatvfsWrapperToFilesystemStatsAdapter(StatvfsWrapper wrapper, long blockSize) {
		this.wrapper = wrapper;
		this.blockSize = blockSize;
	}

	@Override
	public void available(long availableBytes) {
		this.availableBytes = availableBytes;
		delegateIfComplete();
	}

	@Override
	public void used(long usedBytes) {
		this.usedBytes = usedBytes;
		delegateIfComplete();
	}

	private void delegateIfComplete() {
		if (availableBytes == -1 || usedBytes == -1) {
			return;
		}
		wrapper.bsize(blockSize);
		wrapper.bavail(availableBytes / blockSize);
		wrapper.bfree(availableBytes / blockSize);
		wrapper.blocks((availableBytes + usedBytes) / blockSize);
	}

}
