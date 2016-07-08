package org.cryptomator.filesystem.nio;

import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

import org.cryptomator.filesystem.CreateMode;

enum CreateModeToOpenOptionsMapping {

	CREATE_IF_MISSING( //
			CreateMode.CREATE_IF_MISSING, //
			StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE), //

	CREATE_AND_FAIL_IF_PRESENT( //
			CreateMode.CREATE_AND_FAIL_IF_PRESENT, //
			StandardOpenOption.CREATE_NEW, StandardOpenOption.READ, StandardOpenOption.WRITE), //

	FAIL_IF_MISSING( //
			CreateMode.FAIL_IF_MISSING, //
			StandardOpenOption.READ, StandardOpenOption.WRITE)

	;

	public static OpenOption[] openOptionsFor(CreateMode createMode) {
		return valueOf(createMode).options;
	}

	private static CreateModeToOpenOptionsMapping valueOf(CreateMode createMode) {
		for (CreateModeToOpenOptionsMapping result : values()) {
			if (result.mode == createMode) {
				return result;
			}
		}
		throw new IllegalStateException("No option options mapped for create mode " + createMode);
	}

	private final CreateMode mode;
	private final OpenOption[] options;

	private CreateModeToOpenOptionsMapping(CreateMode mode, OpenOption... options) {
		this.mode = mode;
		this.options = options;
	}

}