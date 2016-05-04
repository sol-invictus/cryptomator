package org.cryptomator.frontend.fuse;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.cryptomator.filesystem.FileSystem;
import org.cryptomator.filesystem.nio.NioFileSystem;

import net.fusejna.FuseException;
import net.fusejna.FuseFilesystem;

public class MirroringFuseFilesystemAdapterTest {

	public static void main(String[] args) throws FuseException, IOException {
		if (args.length != 2) {
			System.err.println("Usage: FuseFilesystemAdapterTest <pathToMirroredFileSystem> <mountpoint>");
			System.exit(1);
		}
		FileSystem fileSystem = NioFileSystem.rootedAt(Paths.get(args[0]));
		FuseFilesystem fuseFilesystem = new FuseFilesystemAdapter(fileSystem);
		
		File mountpoint = new File(args[1]);
		try {
			Files.delete(mountpoint.toPath());
		} catch (IOException e) {}
		mountpoint.mkdir();
		fuseFilesystem.mount(mountpoint, false);
		System.out.println(format("Mirroring %s to %s", args[0], args[1]));
		System.out.println("Press any key to unmount...");
		System.in.read();
		fuseFilesystem.unmount();
	}

}
