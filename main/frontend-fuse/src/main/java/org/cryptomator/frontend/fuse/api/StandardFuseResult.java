package org.cryptomator.frontend.fuse.api;

import net.fusejna.ErrorCodes;

public enum StandardFuseResult implements FuseResult {
	SUCCESS(0), //
	IO_ERROR(-ErrorCodes.EIO()), //
	FILE_DOES_NOT_EXIST(-ErrorCodes.ENOENT()), //
	FILE_EXISTS(-ErrorCodes.EEXIST()), //
	INVALID_FILE_HANDLE(-ErrorCodes.EBADF()), //
	UNSUPPORTED_OPERATION(-ErrorCodes.ENODEV()) //

	;

	private final int value;

	private StandardFuseResult(int value) {
		this.value = value;
	}

	@Override
	public int getAsInt() {
		return value;
	}

	@Override
	public String toString() {
		return String.format("%s(%d)", name().toLowerCase(), value);
	}

}
