package org.cryptomator.frontend.fuse.fusejnaadapter;

import org.cryptomator.frontend.fuse.api.Times;

import net.fusejna.StructTimeBuffer.TimeBufferWrapper;

class TimeBufferWrapperToTimesAdapter implements Times {

	private static final long NANOS_PER_MILLI = 1_000_000;
	private TimeBufferWrapper wrapper;

	public TimeBufferWrapperToTimesAdapter(TimeBufferWrapper wrapper) {
		this.wrapper = wrapper;
	}

	@Override
	public void accessTime(long epochMilliseconds) {
		long epochSeconds = epochMilliseconds / 1000;
		long nanosecondOffset = (epochMilliseconds % 1000) * NANOS_PER_MILLI;
		wrapper.ac_set(epochSeconds, nanosecondOffset);
	}

	@Override
	public void modificationTime(long epochMilliseconds) {
		long epochSeconds = epochMilliseconds / 1000;
		long nanosecondOffset = (epochMilliseconds % 1000) * NANOS_PER_MILLI;
		wrapper.mod_set(epochSeconds, nanosecondOffset);
	}

}
