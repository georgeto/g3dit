package de.george.g3utils.structure;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class bCDateTime implements G3Serializable {
	public bCDateTime() {}

	public bCDateTime(long filetime) {
		this.fileTime = filetime;
	}

	private long fileTime;

	@Override
	public void read(G3FileReader reader) {
		fileTime = reader.readLong();
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeLong(fileTime);
	}

	private static final Instant FILETIME_ZERO = Instant.parse("1601-01-01T00:00:00Z");

	public static bCDateTime fromInstant(Instant instant) {
		Duration duration = Duration.between(FILETIME_ZERO, instant);
		return new bCDateTime(duration.getSeconds() * 10_000_000 + duration.getNano() / 100);
	}

	public Instant toInstant() {
		Duration duration = Duration.of(fileTime / 10, ChronoUnit.MICROS).plus(fileTime % 10 * 100, ChronoUnit.NANOS);
		return FILETIME_ZERO.plus(duration);
	}
}
