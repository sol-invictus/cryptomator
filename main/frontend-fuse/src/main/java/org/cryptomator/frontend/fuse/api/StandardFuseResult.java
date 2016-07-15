package org.cryptomator.frontend.fuse.api;

import net.fusejna.ErrorCodes;

public enum StandardFuseResult implements FuseResult {
	SUCCESS(0), //
	IO_ERROR(-ErrorCodes.EIO()), //
	FILE_DOES_NOT_EXIST(-ErrorCodes.ENOENT()), //
	FILE_EXISTS(-ErrorCodes.EEXIST()), //
	IS_DIRECTORY(-ErrorCodes.EISDIR()), //
	IS_NO_DIRECTORY(-ErrorCodes.ENOTDIR()), //
	DIRECTORY_NOT_EMPTY(-ErrorCodes.ENOTEMPTY()), //
	INVALID_FILE_HANDLE(-ErrorCodes.EBADF()), //
	ILLEGAL_ARGUMENTS(-ErrorCodes.EINVAL()), //
	UNSUPPORTED_OPERATION(-ErrorCodes.ENODEV()), //
	CAN_NOT_GROW_FILE_THROUGH_TRUNCATE(-ErrorCodes.EPERM()) //

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
