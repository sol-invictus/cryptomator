package org.cryptomator.frontend.fuse;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.cryptomator.frontend.fuse.impl.NioFuseOperationsFactory.Flag.LOG_DATA;

import java.io.IOException;
import java.nio.file.Paths;

import org.cryptomator.frontend.CommandFailedException;
import org.cryptomator.frontend.Frontend;

public class MirroringFuseFilesystemAdapterTest {

	public static void main(String[] args) throws IOException, CommandFailedException {
		if (args.length != 1) {
			System.err.println("Usage: FuseFilesystemAdapterTest <pathToMirroredFileSystem>");
			System.exit(1);
		}
		
		FuseFrontendFactory frontendFactory = DaggerFuseFrontendComponent.create().fuseFrontendFactory();
		Frontend frontend = frontendFactory.create(Paths.get(args[0]));
		
		frontend.mount(emptyMap());
		System.out.println(format("Mirroring %s", args[0]));
		System.out.println("Press any key to unmount...");
		System.in.read();
		frontend.unmount();
	}

}
