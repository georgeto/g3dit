package de.george.g3dit.jme.asset;

import java.util.Objects;

import com.jme3.asset.MaterialKey;

public class SwitchedMaterialKey extends MaterialKey {
	private int materialSwitch;

	public SwitchedMaterialKey(String name, int materialSwitch) {
		super(name);
		this.materialSwitch = materialSwitch;
	}

	public int getMaterialSwitch() {
		return materialSwitch;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof SwitchedMaterialKey)) {
			return false;
		}
		SwitchedMaterialKey castOther = (SwitchedMaterialKey) other;
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
