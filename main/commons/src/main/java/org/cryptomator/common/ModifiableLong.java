package org.cryptomator.common;

public class ModifiableLong {

	private long value = 0;

	public void add(long value) {
		this.value += value;
	}

	public long get() {
		return value;
	}

}
