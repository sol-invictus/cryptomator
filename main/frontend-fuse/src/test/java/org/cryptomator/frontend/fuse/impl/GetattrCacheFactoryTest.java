package org.cryptomator.frontend.fuse.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CacheBuilder.class)
@SuppressWarnings({ "rawtypes", "unchecked" })
public class GetattrCacheFactoryTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	@Mock
	private CacheBuilderFactory cacheBuilderFactory;
	
	@Mock
	private Function loader;
	
	@Mock
	private CacheBuilder cacheBuilder;
	
	@Mock
	private LoadingCache cache;
	
	private GetattrCacheFactory inTest;
	
	@Before
	public void setup() {
		setupCacheBuilder();
		this.inTest = new GetattrCacheFactory(cacheBuilderFactory);
	}

	private void setupCacheBuilder() {
		when(cacheBuilderFactory.newBuilder()).thenReturn(cacheBuilder);
		when(cacheBuilder.expireAfterWrite(anyLong(), any())).thenReturn(cacheBuilder);
		when(cacheBuilder.maximumSize(anyLong())).thenReturn(cacheBuilder);
		when(cacheBuilder.build(any())).thenReturn(cache);
	}
	
	@Test
	public void testCreateConstructsLoadingCacheWithMaximumSizeOf5000() {
		inTest.create(loader);
		
		verify(cacheBuilder).maximumSize(5000);
	}
	
	@Test
	public void testCreateConstructsLoadingCacheWithTimeoutOf20Seconds() {
		inTest.create(loader);
		
		verify(cacheBuilder).expireAfterWrite(20, TimeUnit.SECONDS);
	}
	
	@Test
	public void testCreateConstructsLoadingCacheUsingCacheLoaderInvokingLoader() throws Exception {
		Path key = mock(Path.class);
		GetattrResult expectedValue = mock(GetattrResult.class);
		when(loader.apply(key)).thenReturn(expectedValue);
		CacheLoader cacheLoader = captureCacheLoader();
		
		Object value = cacheLoader.load(key);
		
		assertThat(value, is(expectedValue));
	}
	
	@Test
	public void testGetReturnsValueFromGetUnchecked() {
		Path key = mock(Path.class);
		GetattrResult expectedValue = mock(GetattrResult.class);
		when(cache.getUnchecked(key)).thenReturn(expectedValue);
		GetattrCache getattrCache = inTest.create(loader);
		
		Object value = getattrCache.get(key);
		
		assertThat(value, is(expectedValue));
	}
	
	@Test
	public void testReloadInvalidatesKeyAndReturnsValueFromGetUnchecked() {
		Path key = mock(Path.class);
		GetattrResult expectedValue = mock(GetattrResult.class);
		when(cache.getUnchecked(key)).thenReturn(expectedValue);
		GetattrCache getattrCache = inTest.create(loader);
		
		Object value = getattrCache.reload(key);
		
		InOrder inOrder = inOrder(cache);
		inOrder.verify(cache).invalidate(key);
		inOrder.verify(cache).getUnchecked(key);
		inOrder.verifyNoMoreInteractions();
		
		assertThat(value, is(expectedValue));
	}

	private CacheLoader captureCacheLoader() {
		ArgumentCaptor<CacheLoader> captor = ArgumentCaptor.forClass(CacheLoader.class);
		inTest.create(loader);
		verify(cacheBuilder).build(captor.capture());
		return captor.getValue();
	}
	
}
