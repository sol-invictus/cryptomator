package org.cryptomator.frontend.fuse.impl;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matcher;
import org.junit.Test;

public class NioAccessModuleTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void testNioAccessModuleCreatesNioAccessImpl() {
		assertThat(NioAccessModule.provideNioAccess(), (Matcher)isA(NioAccessImpl.class));
	}
	
}
