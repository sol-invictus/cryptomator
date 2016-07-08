package org.cryptomator.frontend.fuse.impl;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@Singleton
class GetattrCacheFactory {

	private static final int GET_ATTR_CACHE_SIZE = 5000;
	
	private final CacheBuilderFactory cacheBuilderFactory;
	
	@Inject
	public GetattrCacheFactory(CacheBuilderFactory cacheBuilderFactory) {
		this.cacheBuilderFactory = cacheBuilderFactory;
	}
	
	public GetattrCache create(Function<Path,GetattrResult> loader) {
		return new GetattrCacheImpl(loader);
	}
	
	private final class GetattrCacheImpl implements GetattrCache {

		private final LoadingCache<Path,GetattrResult> delegate;
		
		public GetattrCacheImpl(Function<Path, GetattrResult> loader) {
			delegate = cacheBuilderFactory.newBuilder()
				.expireAfterWrite(20, TimeUnit.SECONDS)
				.maximumSize(GET_ATTR_CACHE_SIZE)
				.build(new CacheLoader<Path,GetattrResult>() {
					@Override
					public GetattrResult load(Path key) {
						return loader.apply(key);
					}
				});
		}

		@Override
		public GetattrResult reload(Path path) {
			delegate.invalidate(path);
			return get(path);
		}

		@Override
		public GetattrResult get(Path path) {
			return delegate.getUnchecked(path);
		}
		
		
		
	}

}
