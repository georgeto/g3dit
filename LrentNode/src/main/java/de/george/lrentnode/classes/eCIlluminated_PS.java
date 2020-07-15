package de.george.lrentnode.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.structure.bCVector;

public class eCIlluminated_PS extends G3Class {

	public StaticLights lights;

	public eCIlluminated_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		lights = new StaticLights(reader);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		int lightCount = lights.getLights().size();
		writer.write("0" + lightCount);
		for (int b = 0; b < 7; b++) {
			for (int i = 0; i < 4; i++) {
				if (i >= lightCount) {
					writer.writeInt(0);
					continue;
				}
				StaticLight light = lights.getLights().get(i);
				switch (b) {
					case 0:
						writer.writeFloat(light.position.getX());
						break;
					case 1:
						writer.writeFloat(light.position.getY());
						break;
					case 2:
						writer.writeFloat(light.position.getZ());
						break;
					case 3:
						writer.writeFloat(light.color.getX());
						break;
					case 4:
						writer.writeFloat(light.color.getY());
						break;
					case 5:
						writer.writeFloat(light.color.getZ());
						break;
					case 6:
						writer.write(light.intensity);
						break;
				}
			}
		}
	}

	public static class StaticLights {
		private List<StaticLight> lights;

		public StaticLights(G3FileReader reader) {
			int count = reader.readByte() & 0xFF;
			lights = new ArrayList<>(count);
			for (int i = 0; i < count; i++) {
				StaticLight light = new StaticLight();
				light.position = new bCVector(
						reader.readSilent(i * 4, 4) + reader.readSilent(i * 4 + 16, 4) + reader.readSilent(i * 4 + 32, 4));
				light.color = new bCVector(
						reader.readSilent(i * 4 + 48, 4) + reader.readSilent(i * 4 + 64, 4) + reader.readSilent(i * 4 + 80, 4));
				light.intensity = reader.readSilent(i * 4 + 96, 4);
				lights.add(light);
			}
			reader.skip(112);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof StaticLights)) {
				return false;
			}
			StaticLights other = (StaticLights) obj;
			if (lights == null) {
				if (other.lights != null) {
					return false;
				}
			} else if (!lights.equals(other.lights)) {
				return false;
			}
			return true;
		}

		public List<StaticLight> getLights() {
			return lights;
		}

		public void setLights(List<StaticLight> lights) {
			this.lights = lights;
		}
	}

	public static class StaticLight {
		public bCVector position;
		public bCVector color;
		public String intensity;

		@Override
		public boolean equals(final Object other) {
			if (!(other instanceof StaticLight)) {
				return false;
			}
			StaticLight castOther = (StaticLight) other;
			return Objects.equals(position, castOther.position) && Objects.equals(color, castOther.color)
					&& Objects.equals(intensity, castOther.intensity);
		}

		@Override
		public int hashCode() {
			return Objects.hash(position, color, intensity);
		}
	}
}
