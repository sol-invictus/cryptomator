package org.cryptomator.frontend.fuse.api;

import java.util.function.IntSupplier;

public interface FuseResult extends IntSupplier {

	static FuseResult withValue(int value) {
		return new FuseResult() {
			@Override
			public int getAsInt() {
				return value;
			}

			@Override
			public String toString() {
				return String.format("value(%d)", value);
			}
		};
	}

}
