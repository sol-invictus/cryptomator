package org.cryptomator.filesystem.nio;

import static org.cryptomator.filesystem.nio.CreateModeToOpenOptionsMapping.openOptionsFor;
import static org.junit.Assert.assertNotNull;

import org.cryptomator.filesystem.CreateMode;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class CreateModeToOpenOptionsMappingTest {

	@Theory
	public void testOptionsAreAvailableForCreateMode(CreateMode createMode) {
		assertNotNull(openOptionsFor(createMode));
	}

}
