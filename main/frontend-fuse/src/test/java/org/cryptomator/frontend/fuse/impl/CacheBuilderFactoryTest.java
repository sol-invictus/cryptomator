package org.cryptomator.frontend.fuse.impl;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.common.cache.CacheBuilder;

public class CacheBuilderFactoryTest {

	private CacheBuilderFactory inTest = new CacheBuilderFactory();
	
	@Test
	public void testCacheBuilderFactoryCreatesCacheBuilder() {
		CacheBuilder<String,Object> builder = inTest.newBuilder();
		
		assertThat(builder, is(notNullValue()));
	}
	
	@Test
	public void testCacheBuilderFactoryCreatesNewCacheBuilder() {
		CacheBuilder<String,Object> builder1 = inTest.newBuilder();
		CacheBuilder<String,Object> builder2 = inTest.newBuilder();
		
		assertThat(builder1, is(not(builder2)));
	}
	
}
