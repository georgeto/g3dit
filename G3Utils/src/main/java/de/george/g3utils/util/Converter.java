package de.george.g3utils.util;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.List;

public class Converter {
	public static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

	public static float HexBigToFloat(String hex) {
		long l = Long.parseLong(hex, 16);
		return Float.intBitsToFloat((int) l);
	}

	public static String FloatToHexBig(float f) {
		int i = Float.floatToIntBits(f);
		String temp = Integer.toHexString(i).toUpperCase();
		if (temp.equalsIgnoreCase("0")) {
			temp = "00000000";
		}
		return temp;
	}

	/**
	 * Das Genome Format verwendet den LittleEndian
	 *
	 * @param w
	 * @return
	 */
	public static float HexLittleToFloat(String hex) {
		hex = revertEndian(hex);
		long l = Long.parseLong(hex, 16);
		return Float.intBitsToFloat((int) l);
	}

	public static String FloatToHexLittle(float f) {
		int i = Float.floatToIntBits(f);
		String temp = Integer.toHexString(i).toUpperCase();
		if (temp.equalsIgnoreCase("0")) {
			temp = "00000000";
		}
		return revertEndian(temp);
	}

	public static int WordBigToInt(String w) {
		return Long.valueOf(w, 16).intValue();
	}

	public static String IntToWordBig(int i, int dest) {
		String temp = Integer.toHexString(i).toUpperCase();
		for (int j = temp.length(); j < dest; j++) {
			temp = "0" + temp;
		}
		return temp;
	}

	/**
	 * Das Genome Format verwendet den LittleEndian
	 *
	 * @param w
	 * @return
	 */
	public static int WordLittleToInt(String w) {
		return Long.valueOf(revertEndian(w), 16).intValue();
	}

	public static String IntToWordLittle(int i, int dest) {
		String temp = Integer.toHexString(i).toUpperCase();
		for (int j = temp.length(); j < dest; j++) {
			temp = "0" + temp;
		}
		return revertEndian(temp);
	}

	private static final String[] buffer = {"0", "00", "000", "0000", "00000", "000000", "0000000", "00000000"};

	public static String IntToHexLittle(int i, int dest) {
		String temp = Integer.toHexString(i).toUpperCase();
		int dist = dest - temp.length();
		if (dist > 0) {
			return revertEndian(buffer[dist - 1] + temp);
		}
		return revertEndian(temp);
	}

	public static long WordBigToLong(String w) {
		return Long.parseLong(w, 16);
	}

	public static String LongToWordBig(long i, int dest) {
		String temp = Long.toHexString(i).toUpperCase();
		for (int j = temp.length(); j < dest; j++) {
			temp = "0" + temp;
		}
		return temp;
	}

	public static long WordLittleToLong(String w) {
		return Long.parseLong(revertEndian(w), 16);
	}

	public static String LongToWordLittle(long i, int dest) {
		String temp = Long.toHexString(i).toUpperCase();
		for (int j = temp.length(); j < dest; j++) {
			temp = "0" + temp;
		}
		return revertEndian(temp);
	}

	private static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static String hexToString(String str) {
		return byteArrayToString(hexStringToByteArray(str));
	}

	public static String byteArrayToString(byte[] bytes) {
		return new String(bytes, WINDOWS_1252);
	}

	public static String stringToHex(String arg) {
		return String.format("%0" + arg.length() * 2 + "x", new BigInteger(1, arg.getBytes(WINDOWS_1252))).toUpperCase();
	}

	public static byte[] stringToByteArray(String arg) {
		return arg.getBytes(WINDOWS_1252);
	}

	public static String revertEndian(String w) {
		byte[] bytes = Misc.asByte(w);
		byte[] result = new byte[bytes.length];
		for (int i = bytes.length - 1; i >= 0; i--) {
			result[bytes.length - i - 1] = bytes[i];
		}

		return Misc.asHex(result);
	}

	public static String FloatArrayToHexLittle(float... floats) {
		StringBuffer buffer = new StringBuffer(floats.length * 8);
		for (float part : floats) {
			buffer.append(Converter.FloatToHexLittle(part));
		}
		return buffer.toString();
	}

	public static String FloatListToHexLittle(List<Float> floats) {
		StringBuffer buffer = new StringBuffer(floats.size() * 8);
		for (float part : floats) {
			buffer.append(Converter.FloatToHexLittle(part));
		}
		return buffer.toString();
	}

	public static String IntArrayToHexLittle(int... ints) {
		return IntArrayToHexLittle(8, ints);
	}

	public static String IntArrayToHexLittle(int dest, int... ints) {
		StringBuffer buffer = new StringBuffer(ints.length * dest);
		for (int part : ints) {
			buffer.append(Converter.IntToHexLittle(part, dest));
		}
		return buffer.toString();
	}

	public static String IntListToHexLittle(List<Integer> ints) {
		return IntListToHexLittle(8, ints);
	}

	public static String IntListToHexLittle(int dest, List<Integer> ints) {
		StringBuffer buffer = new StringBuffer(ints.size() * dest);
		for (int part : ints) {
			buffer.append(Converter.IntToHexLittle(part, dest));
		}
		return buffer.toString();
	}

	public static String BoolToHex(boolean bool) {
		return bool ? "01" : "00";
	}
}
