package de.george.g3dit.jme.asset;

import java.util.Objects;

import com.jme3.asset.ModelKey;

public class SwitchedModelKey extends ModelKey {
	private int materialSwitch;

	public SwitchedModelKey(String name, int materialSwitch) {
		super(name);
		this.materialSwitch = materialSwitch;
	}

	public int getMaterialSwitch() {
		return materialSwitch;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof SwitchedModelKey castOther)) {
			return false;
		}
		return Objects.equals(name, castOther.name) && Objects.equals(materialSwitch, castOther.materialSwitch);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, materialSwitch);
	}

	@Override
	public String toString() {
		return name + "[" + materialSwitch + "]";
	}
}
