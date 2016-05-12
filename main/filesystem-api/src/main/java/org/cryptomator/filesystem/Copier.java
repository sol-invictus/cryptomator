/*******************************************************************************
 * Copyright (c) 2015 Sebastian Stenzel and others.
 * This file is licensed under the terms of the MIT license.
 * See the LICENSE.txt file for more info.
 *
 * Contributors:
 *     Sebastian Stenzel - initial API and implementation
 *******************************************************************************/
package org.cryptomator.filesystem;

import static org.cryptomator.filesystem.CreateMode.CREATE_IF_MISSING;

import org.cryptomator.io.FileUtils;

class Copier {

	public static void copy(Folder source, Folder destination) {
		assertFoldersAreNotNested(source, destination);

		destination.delete();
		destination.create();

		source.files().forEach(sourceFile -> {
			File destinationFile = destination.file(sourceFile.name());
			sourceFile.copyTo(destinationFile);
		});

		source.folders().forEach(sourceFolder -> {
			Folder destinationFolder = destination.folder(sourceFolder.name());
			sourceFolder.copyTo(destinationFolder);
		});
	}

	public static void copy(File source, File destination) {
		try (ReadableFile readable = source.openReadable(); //
				WritableFile writable = destination.openWritable(CREATE_IF_MISSING)) {
			writable.truncate();
			FileUtils.copy(readable, writable);
		}
	}

	private static void assertFoldersAreNotNested(Folder source, Folder destination) {
		if (source.isAncestorOf(destination)) {
			throw new IllegalArgumentException("Can not copy parent to child directory (src: " + source + ", dst: " + destination + ")");
		}
		if (destination.isAncestorOf(source)) {
			throw new IllegalArgumentException("Can not copy child to parent directory (src: " + source + ", dst: " + destination + ")");
		}
	}

}
