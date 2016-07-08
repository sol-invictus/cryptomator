package org.cryptomator.frontend.fuse.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.CacheBuilder;

@Singleton
class CacheBuilderFactory {

	@Inject
	CacheBuilderFactory() {}
	
	@SuppressWarnings("unchecked")
	public <K,V> CacheBuilder<K,V> newBuilder() {
		return (CacheBuilder<K,V>)CacheBuilder.newBuilder();
	}
	
}
