package org.cryptomator.frontend.fuse;

import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.isRegularFile;
import static org.cryptomator.frontend.Frontend.MountParam.MOUNT_NAME;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import org.cryptomator.frontend.CommandFailedException;
import org.cryptomator.frontend.Frontend;

import net.fusejna.FuseException;
import net.fusejna.FuseFilesystem;

class FuseFrontend implements Frontend {

	private final FuseFilesystem fuseJnaFilesystem;
	
	public FuseFrontend(FuseFilesystem fuseJnaFilesystem) {
		this.fuseJnaFilesystem = fuseJnaFilesystem;
	}

	@Override
	public void mount(Map<MountParam, Optional<String>> map) throws CommandFailedException {
		try {
			Path mountLocation = Paths.get(map.get(MOUNT_NAME).get());
			if (!isDirectory(mountLocation) && !isRegularFile(mountLocation)) {
				deleteIfExists(mountLocation);
				createDirectories(mountLocation);
			}
			fuseJnaFilesystem.mount(mountLocation.toFile(), false);
		} catch (FuseException | IOException e) {
			throw new CommandFailedException(e);
		}
	}

	@Override
	public void unmount() throws CommandFailedException {
		try {
			fuseJnaFilesystem.unmount();
		} catch (IOException | FuseException e) {
			throw new CommandFailedException(e);
		}
	}

	@Override
	public void reveal() throws CommandFailedException {
		// TODO fixme
	}

	@Override
	public void close() throws CommandFailedException {
		if (fuseJnaFilesystem.isMounted()) {
			unmount();
		}
	}

	@Override
	public String getWebDavUrl() {
		// TODO API FAIL
		return "";
	}

}
