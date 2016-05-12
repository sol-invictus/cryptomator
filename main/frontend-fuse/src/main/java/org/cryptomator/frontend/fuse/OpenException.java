package org.cryptomator.frontend.fuse;

import net.fusejna.ErrorCodes;

class OpenException extends Exception {

	public static enum Reason {
		
		FILE_DOES_NOT_EXIST(-ErrorCodes.ENOENT()),
		FILE_IS_A_DIRECTORY(-ErrorCodes.EISDIR()),
		PARENT_DOES_NOT_EXIST(-ErrorCodes.ENOENT())
		
		;
		
		private final int errorCode;
		
		private Reason(int errorCode) {
			this.errorCode = errorCode;
		}
		
		public String toString() {
			return name().toLowerCase().replace('_', ' ');
		}

		public int toErrorCode() {
			return errorCode;
		}
	}
	
	private final Reason reason;
	
	public OpenException(Reason reason) {
		super(reason.toString());
		this.reason = reason;
	}
	
	public Reason reason() {
		return reason;
	}
	
}
