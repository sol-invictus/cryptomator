package org.cryptomator.frontend.fuse;

import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.cryptomator.frontend.Frontend;
import org.cryptomator.frontend.fuse.api.FuseOperations;
import org.cryptomator.frontend.fuse.fusejnaadapter.FuseFilesystemToFuseJnaAdapterFactory;
import org.cryptomator.frontend.fuse.impl.NioFuseOperationsFactory;
import org.cryptomator.frontend.fuse.impl.NioFuseOperationsFactory.Flag;

import net.fusejna.FuseFilesystem;

@Singleton
public class FuseFrontendFactory {

	private final NioFuseOperationsFactory nioFuseOperationsFactory;
	private final FuseFilesystemToFuseJnaAdapterFactory fuseFilesystemToFuseJnaAdapterFactory;
	
	@Inject
	FuseFrontendFactory(NioFuseOperationsFactory nioFuseOperationsFactory, FuseFilesystemToFuseJnaAdapterFactory fuseFilesystemToFuseJnaAdapterFactory) {
		this.nioFuseOperationsFactory = nioFuseOperationsFactory;
		this.fuseFilesystemToFuseJnaAdapterFactory = fuseFilesystemToFuseJnaAdapterFactory;
	}
	
	public Frontend create(Path root, Flag ... flags) {
		FuseOperations fuseOperations = nioFuseOperationsFactory.create(root, flags);
		org.cryptomator.frontend.fuse.api.FuseFilesystem fuseFilesystem = () -> fuseOperations;
		FuseFilesystem fuseJnaFilesystem = fuseFilesystemToFuseJnaAdapterFactory.create(fuseFilesystem);
		return new FuseFrontend(fuseJnaFilesystem);
	}
	
}
