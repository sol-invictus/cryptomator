package org.cryptomator.frontend.fuse;

import static java.lang.String.format;
import static org.cryptomator.frontend.Frontend.MountParam.MOUNT_NAME;
import static org.cryptomator.frontend.fuse.impl.NioFuseOperationsFactory.Flag.LOG_DATA;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.cryptomator.frontend.CommandFailedException;
import org.cryptomator.frontend.Frontend;
import org.cryptomator.frontend.Frontend.MountParam;

public class MirroringFuseFilesystemAdapterTest {

	public static void main(String[] args) throws IOException, CommandFailedException {
		Args parsedArgs = parse(args);
		
		FuseFrontendFactory frontendFactory = DaggerFuseFrontendComponent.create().fuseFrontendFactory();
		Frontend frontend = frontendFactory.create(parsedArgs.mirroredDirectory(), LOG_DATA);
		
		Map<MountParam,Optional<String>> params = new HashMap<>();
		params.put(MOUNT_NAME, Optional.of(parsedArgs.mountDirectory().toString()));
		
		frontend.mount(params);
		
		System.out.println(format("Mirroring %s to %s", args[0], args[1]));
		System.out.println("Press any key to unmount...");
		System.in.read();
		
		frontend.unmount();
	}

	private static Args parse(String[] args) throws IOException {
		try {
			return new Args(args);
		} catch (ArgsException e) {
			System.err.print(e.getMessage());
			System.err.println();
			System.err.println("Usage: FuseFilesystemAdapterTest <mirroredDirectory> <mountDirectory>");
			System.exit(1);
			throw new IllegalStateException();
		}
	}
	
	private static class Args {
		
		private final Path mirroredDirectory;
		private final Path mountDirectory;
		
		public Args(String[] args) throws ArgsException, IOException {
			if (args.length != 2) {
				throw new ArgsException("Invalid number of arguments");
			}
			mirroredDirectory = Paths.get(args[0]);
			mountDirectory = Paths.get(args[1]);
			validate();
		}

		private void validate() throws ArgsException, IOException {
			if (!Files.isDirectory(mirroredDirectory)) {
				throw new ArgsException(mirroredDirectory + " does not exist");
			}
			if (Files.isDirectory(mountDirectory)) {
				try (Stream<Path> contents = Files.list(mountDirectory)) {
					if (contents.count() > 0) {
						throw new ArgsException(mountDirectory + " exists and is not empty");
					}
				}
			}
		}
		
		public Path mirroredDirectory() {
			return mirroredDirectory;
		}
		
		public Path mountDirectory() {
			return mountDirectory;
		}
		
	}

}
