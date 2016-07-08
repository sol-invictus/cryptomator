package org.cryptomator.frontend.fuse.fusejnaadapter;

import org.cryptomator.frontend.fuse.api.Attributes;

import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;

class StatWrapperToAttributesAdapter implements Attributes {

	private static final long NANOS_PER_MILLI = 1_000_000;
	private final StatWrapper stat;

	public StatWrapperToAttributesAdapter(StatWrapper stat) {
		this.stat = stat;
	}

	@Override
	public void accessTime(long epochMilliseconds) {
		long epochSeconds = epochMilliseconds / 1000;
		long nanosecondOffset = (epochMilliseconds % 1000) * NANOS_PER_MILLI;
		stat.atime(epochSeconds, nanosecondOffset);
	}

	@Override
	public void modificationTime(long epochMilliseconds) {
		long epochSeconds = epochMilliseconds / 1000;
		long nanosecondOffset = (epochMilliseconds % 1000) * NANOS_PER_MILLI;
		stat.mtime(epochSeconds, nanosecondOffset);
	}

	@Override
	public void file() {
		stat.setMode(NodeType.FILE);
	}

	@Override
	public void folder() {
		stat.setMode(NodeType.DIRECTORY);
	}

	@Override
	public void size(long size) {
		stat.size(size);
	}

	@Override
	public void creationTime(long epochMilliseconds) {
		long epochSeconds = epochMilliseconds / 1000;
		long nanosecondOffset = (epochMilliseconds % 1000) * NANOS_PER_MILLI;
		stat.ctime(epochSeconds, nanosecondOffset);
	}

}
