
package de.george.g3dit;

import org.junit.Assert;
import org.junit.Test;

import de.george.lrentnode.enums.G3Enums.eEColorSrcSwitchRepeat;

public class MaterialSwitchTest {
	public static String getEnding(int materialSwitch, int textureCount, int switchRepeat) {
		int textureIndex = 0;
		switch (switchRepeat) {
			case eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Repeat:
				textureIndex = materialSwitch % textureCount;
				break;

			case eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Clamp:
				textureIndex = materialSwitch;
				if (textureIndex < 0) {
					textureIndex = 0;
				}
				if (textureIndex > textureCount - 1) {
					textureIndex = textureCount - 1;
				}
				break;

			case eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_PingPong:
				textureIndex = materialSwitch % textureCount;
				if ((textureIndex & 1) == 1) {
					textureIndex = textureCount - textureIndex - 1;
				}
				break;
		}

		return "S" + (textureIndex + 1);
	}

	@Test
	public void testRepeat() {
		Assert.assertEquals("S1", getEnding(0, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Repeat));
		Assert.assertEquals("S2", getEnding(1, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Repeat));
		Assert.assertEquals("S3", getEnding(2, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Repeat));
		Assert.assertEquals("S4", getEnding(3, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Repeat));
		Assert.assertEquals("S5", getEnding(4, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Repeat));
		Assert.assertEquals("S1", getEnding(5, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Repeat));
		Assert.assertEquals("S2", getEnding(6, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Repeat));
	}

	@Test
	public void testClamp() {
		Assert.assertEquals("S1", getEnding(-1, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Clamp));
		Assert.assertEquals("S1", getEnding(0, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Clamp));
		Assert.assertEquals("S2", getEnding(1, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Clamp));
		Assert.assertEquals("S3", getEnding(2, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Clamp));
		Assert.assertEquals("S4", getEnding(3, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Clamp));
		Assert.assertEquals("S5", getEnding(4, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Clamp));
		Assert.assertEquals("S5", getEnding(5, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Clamp));
		Assert.assertEquals("S5", getEnding(6, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_Clamp));
	}

	@Test
	public void testPingPong() {
		Assert.assertEquals("S1", getEnding(0, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_PingPong));
		Assert.assertEquals("S4", getEnding(1, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_PingPong));
		Assert.assertEquals("S3", getEnding(2, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_PingPong));
		Assert.assertEquals("S2", getEnding(3, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_PingPong));
		Assert.assertEquals("S5", getEnding(4, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_PingPong));
		Assert.assertEquals("S1", getEnding(5, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_PingPong));
		Assert.assertEquals("S4", getEnding(6, 5, eEColorSrcSwitchRepeat.eEColorSrcSwitchRepeat_PingPong));
	}
}
