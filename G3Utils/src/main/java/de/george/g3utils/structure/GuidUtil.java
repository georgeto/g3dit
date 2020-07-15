package de.george.g3utils.structure;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import com.google.common.hash.Hashing;

import de.george.g3utils.util.Converter;
import de.george.g3utils.util.Misc;

public class GuidUtil {

	public static String hexToGroup(String hex) {
		if (hex != null && hex.length() == 40 && Misc.isValidHex(hex)) {
			String part1 = Converter.revertEndian(hex.substring(0, 8));
			String part2 = Converter.revertEndian(hex.substring(8, 12));
			String part3 = Converter.revertEndian(hex.substring(12, 16));
			String part4 = hex.substring(16, 20);
			String part5 = hex.substring(20, 32);
			long counter = Converter.WordLittleToLong(hex.substring(32, 40));
			return (part1 + "-" + part2 + "-" + part3 + "-" + part4 + "-" + part5 + ":" + counter).toLowerCase();
		}

		return null;
	}

	public static String hexToPlain(String hex) {
		String trimmedHex = trimGuid(hex);
		if (trimmedHex != null && (trimmedHex.length() == 32 || trimmedHex.length() == 40)) {
			return Converter.hexToString(trimmedHex);
		} else {
			return null;
		}
	}

	public static String hexToLrtpldatasc(String hex) {
		String group = hexToGroup(hex);
		return group != null ? "{" + group.replace(":", "}:") : null;
	}

	public static String hexToGuidText(String hex) {
		String group = hexToGroup(hex);
		return group != null ? "{" + group.replaceFirst(":.*", "}") : null;
	}

	public static String deriveGUID(String seed) {
		Random random = new Random(Hashing.sha256().hashString(seed, StandardCharsets.UTF_8).asLong());
		return Converter.stringToHex("CSPG" + Misc.randomString(12, random)) + "00000000";
	}

	public static String randomGUID() {
		return Converter.stringToHex("CSPG" + Misc.randomString(12)) + "00000000";
	}

	public static boolean isValid(String rawGuid) {
		return parseGuid(rawGuid) != null;
	}

	public static boolean isValidOrEmpty(String rawGuid) {
		if (rawGuid == null || rawGuid.isEmpty()) {
			return true;
		}
		return parseGuid(rawGuid) != null;
	}

	public static String parseGuid(String rawGuid) {
		if (rawGuid == null || rawGuid.isEmpty()) {
			return null;
		}
		// Hexadezimale Darstellung mit bzw. ohne explizite Angange des Counters
		if ((rawGuid.length() == 40 || rawGuid.length() == 32) && Misc.isValidHex(rawGuid)) {
			return rawGuid + (rawGuid.length() == 32 ? "00000000" : "");
		} else if (rawGuid.length() == 16) {
			return Converter.stringToHex(rawGuid) + "00000000";
		} else if (rawGuid.length() == 20) {
			return Converter.stringToHex(rawGuid);
		} else if (rawGuid.length() == 36
				&& rawGuid.matches("[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}")) {
			return groupToHex(rawGuid) + "00000000";
		} else if (rawGuid.length() == 38
				&& rawGuid.matches("\\{[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}\\}")) {
			return groupToHex(rawGuid.substring(1, rawGuid.length() - 1)) + "00000000";
		} else if (rawGuid.length() >= 38
				&& rawGuid.matches("[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}:\\d{1,10}")) {
			String[] counterSplit = rawGuid.split(":");
			Long counter = Long.valueOf(counterSplit[1]);
			return counter < 0 || counter > 4294967295L ? null
					: groupToHex(counterSplit[0]) + Converter.LongToWordLittle(counter, 8).toUpperCase();
		}
		return null;
	}

	public static String parseGuidPartial(String rawGuid) {
		String guid = GuidUtil.parseGuid(rawGuid);
		if (guid == null && rawGuid != null && !rawGuid.isEmpty()) { // partial Guid
			guid = rawGuid.toUpperCase();
			if (!Misc.isValidHex(guid)) {
				guid = Converter.stringToHex(rawGuid);
			}
		}
		return guid;
	}

	public static String trimGuid(String guid) {
		if (guid != null && guid.length() == 40 && guid.matches("[0-9A-Fa-f]{32}0{8}")) {
			return guid.substring(0, 32);
		} else {
			return guid;
		}
	}

	private static String groupToHex(String group) {
		String[] split = group.split("-");
		return (Converter.revertEndian(split[0]) + Converter.revertEndian(split[1]) + Converter.revertEndian(split[2]) + split[3]
				+ split[4]).toUpperCase();
	}
}
