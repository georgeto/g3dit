package de.george.navmap.sections;

import java.nio.file.Paths;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;

import de.george.g3utils.io.G3FileReaderEx;

public class NavMapTest {

	protected NavMap navMap;

	@Before
	public void loadNavMap() throws Exception {
		try (G3FileReaderEx reader = new G3FileReaderEx(Paths.get("NavigationMap.xnav"))) {
			navMap = new NavMap(reader);
		} catch (Exception e) {
		}

		// Skip if no NavMap was found.
		Assume.assumeNotNull(navMap);
	}

	@After
	public void clearNavMap() {
		navMap = null;
	}
}
