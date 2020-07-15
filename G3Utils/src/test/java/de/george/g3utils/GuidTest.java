package de.george.g3utils;

import org.junit.Assert;
import org.junit.Test;

import de.george.g3utils.structure.GuidUtil;

public class GuidTest {
	@Test
	public void testIllegalChar() {
		String guid = GuidUtil.parseGuid("1664D47482E1EB4F9CB1F7AD72F3255A");
		Assert.assertNotNull(guid);
		Assert.assertEquals("1664D47482E1EB4F9CB1F7AD72F3255A", GuidUtil.trimGuid(guid));
		Assert.assertEquals(guid, GuidUtil.parseGuid(GuidUtil.hexToPlain(guid)));
	}

	@Test
	public void testCounter() {
		String guid = GuidUtil.parseGuid("C1E3A2D0-F1F3-448A-B0FC-30C496D2D353:1");
		Assert.assertNotNull(guid);
		Assert.assertEquals(GuidUtil.hexToGroup(guid), "c1e3a2d0-f1f3-448a-b0fc-30c496d2d353:1");
	}

	@Test
	public void testCounter2() {
		String guid = GuidUtil.parseGuid("c1e3a2d0-f1f3-448a-b0fc-30c496d2d353:4294967295");
		Assert.assertNotNull(guid);
		Assert.assertEquals(GuidUtil.hexToGroup(guid), "c1e3a2d0-f1f3-448a-b0fc-30c496d2d353:4294967295");
	}
}
