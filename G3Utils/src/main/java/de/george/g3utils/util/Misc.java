package de.george.g3utils.util;

import java.awt.Color;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCVector;
import one.util.streamex.StreamEx;

public class Misc {

	private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final Random rnd = new Random();

	public static String randomString(int len) {
		return randomString(len, rnd);
	}

	public static String randomString(int len, Random random) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(AB.charAt(random.nextInt(AB.length())));
		}
		return sb.toString();
	}

	private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

	public static String asHex(byte... buf) {
		char[] chars = new char[2 * buf.length];
		for (int i = 0; i < buf.length; ++i) {
			chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
			chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
		}
		return new String(chars);
	}

	public static byte[] asByte(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public static boolean isValidHex(String hex) {
		String upHex = hex.toUpperCase();
		for (int i = 0; i < upHex.length(); i++) {
			if (Arrays.binarySearch(HEX_CHARS, upHex.charAt(i)) < 0) {
				return false;
			}
		}
		return true;
	}

	public static float round(float value, int comma) {
		double multi = Math.pow(10, comma);
		return (float) (Math.round(value * multi) / multi);
	}

	public static String cutString(String source) {
		String result = "";
		int lastPos = 0;
		for (int i = source.length(); i > 0; i -= 58) {
			if (i >= 58) {
				result += source.substring(lastPos, lastPos + 58) + "\n";
				lastPos += 58;
			} else {
				result += source.substring(lastPos);
			}
		}
		return result;
	}

	/**
	 * Finds the first occurrence of the pattern in the text.
	 */
	public static int indexOf(byte[] data, byte[] pattern) {
		return indexOf(data, pattern, 0);
	}

	/**
	 * Finds the first occurrence after the offset of the pattern in the text.
	 */
	public static int indexOf(byte[] data, byte[] pattern, int offset) {
		int[] failure = computeFailure(pattern);

		int j = 0;
		if (data.length == 0) {
			return -1;
		}

		for (int i = offset; i < data.length; i++) {
			while (j > 0 && pattern[j] != data[i]) {
				j = failure[j - 1];
			}
			if (pattern[j] == data[i]) {
				j++;
			}
			if (j == pattern.length) {
				return i - pattern.length + 1;
			}
		}
		return -1;
	}

	/**
	 * Computes the failure function using a boot-strapping process, where the pattern is matched
	 * against itself.
	 */
	private static int[] computeFailure(byte[] pattern) {
		int[] failure = new int[pattern.length];

		int j = 0;
		for (int i = 1; i < pattern.length; i++) {
			while (j > 0 && pattern[j] != pattern[i]) {
				j = failure[j - 1];
			}
			if (pattern[j] == pattern[i]) {
				j++;
			}
			failure[i] = j;
		}

		return failure;
	}

	public static String formatFloat(float f) {
		int i = (int) f;
		return f == i ? String.valueOf(i) : String.valueOf(f);
	}

	public static String formatFloat(float f, int comma) {
		return formatFloat(round(f, comma));
	}

	public static String positionToPrettyString(bCVector position, bCEulerAngles rotation, bCVector scaling) {
		String result = "x: " + formatFloat(position.getX()) + "  y: " + formatFloat(position.getY()) + "  z: "
				+ formatFloat(position.getZ());

		result += "\npitch: " + formatFloat(rotation.getPitchDeg()) + "  yaw: " + formatFloat(rotation.getYawDeg()) + "  roll: "
				+ formatFloat(rotation.getRollDeg());

		// Uniform scaling
		if (scaling.getX() == scaling.getY() && scaling.getX() == scaling.getZ()) {
			result += "\nscale: " + formatFloat(scaling.getX());
		} else {
			result += "\nscalex: " + formatFloat(scaling.getX()) + "  scaley: " + formatFloat(scaling.getY()) + "  scalez: "
					+ formatFloat(scaling.getZ());
		}

		return result;
	}

	public static String positionToString(bCMatrix matrix) {
		return positionToString(matrix.getTranslation(), new bCEulerAngles(matrix), matrix.getPureScaling());
	}

	public static String positionToString(bCVector position, bCEulerAngles rotation, bCVector scaling) {
		String result = position.toString().replace(".0/", "/") + "\npitch: " + formatFloat(rotation.getPitchDeg()) + " yaw: "
				+ formatFloat(rotation.getYawDeg()) + " roll: " + formatFloat(rotation.getRollDeg());

		// Uniform scaling
		if (scaling.getX() == scaling.getY() && scaling.getX() == scaling.getZ()) {
			result += "\nscale: " + formatFloat(scaling.getX());
		} else {
			result += "\nscalex: " + formatFloat(scaling.getX()) + " scaley: " + formatFloat(scaling.getY()) + " scalez: "
					+ formatFloat(scaling.getZ());
		}

		return result;
	}

	private static final String FLOAT_REGEX = "[-+]?([0-9]*[.])?[0-9]+([eE][-+]?\\d+)?";

	public static Optional<bCMatrix> stringToMatrix(String text) {
		bCVector position = Misc.stringToPosition(text);
		if (position != null) {
			bCMatrix matrix = new bCMatrix();
			matrix.modifyRotation(Misc.stringToRotation(text));
			matrix.modifyScaling(Misc.stringToScaling(text));
			matrix.modifyTranslation(position);
			return Optional.of(matrix);
		}
		return Optional.empty();
	}

	public static bCVector stringToPosition(String text) {
		String temp = Misc.regexSearch(FLOAT_REGEX + "/" + FLOAT_REGEX + "/" + FLOAT_REGEX, text);
		if (temp == null) {
			return null;
		}
		String[] splited = temp.split("/");
		return new bCVector(Float.valueOf(splited[0].replace(",", ".")), Float.valueOf(splited[1].replace(",", ".")),
				Float.valueOf(splited[2].replace(",", ".")));
	}

	public static bCEulerAngles stringToRotation(String text) {
		float pitch = Misc.stringToPrefixedValue(text, 0, "pitch");
		float yaw = Misc.stringToPrefixedValue(text, 0, "yaw");
		float roll = Misc.stringToPrefixedValue(text, 0, "roll");
		return bCEulerAngles.fromDegree(yaw, pitch, roll);
	}

	public static bCVector stringToScaling(String text) {
		float scaleX = Misc.stringToPrefixedValue(text, 1, "sx", "scalex", "scale");
		float scaleY = Misc.stringToPrefixedValue(text, 1, "sy", "scaley", "scale");
		float scaleZ = Misc.stringToPrefixedValue(text, 1, "sz", "scalez", "scale");
		return new bCVector(scaleX, scaleY, scaleZ);
	}

	public static Color stringToColor(String text) {
		String rawColor = Misc.stringToPrefixedValue(text, null, "color", "farbe", "cl");
		if (rawColor == null) {
			return null;
		}
		if (!rawColor.startsWith("0x") && !rawColor.startsWith("0X")) {
			rawColor = "0x" + rawColor;
		}
		Color color = null;
		try {
			color = new Color(Long.decode(rawColor).intValue());
		} catch (NumberFormatException e) {
		}
		return color;
	}

	public static float stringToPrefixedValue(String text, float defaultValue, String... prefixes) {
		String aliases = String.join("|", prefixes);
		return Misc.regexSearchFloat("(?i)(?<=(" + aliases + "):\\s{0,10})" + FLOAT_REGEX, text, defaultValue);
	}

	public static String stringToPrefixedValue(String text, String defaultValue, String... prefixes) {
		String aliases = String.join("|", prefixes);
		String result = Misc.regexSearch("(?i)(?<=(" + aliases + "):\\s{0,10})\\w+", text);
		return result != null ? result : defaultValue;
	}

	public static Map<String, String> stringValueListToMap(String text, String pairSeparator, String keyValueSeparator) {
		return Arrays.stream(text.replaceAll("\\s", "").split(pairSeparator)).map(p -> p.split(keyValueSeparator))
				.filter(p -> p.length == 2).collect(Collectors.toMap(p -> p[0], p -> p[1]));
	}

	public static <T> List<T> parseList(String text, Function<String, T> parser) throws IllegalArgumentException {
		return Arrays.stream(text.split("\\n")).map(parser).collect(Collectors.toList());
	}

	public static List<String> parseList(String text) {
		return Arrays.stream(text.split("\\n")).collect(Collectors.toList());
	}

	public static List<bCVector> parseVectorList(String text) throws IllegalArgumentException {
		return parseList(text, bCVector::fromString);
	}

	public static List<Float> parseFloatList(String text) throws IllegalArgumentException {
		return parseList(text, Misc::parseFloat);
	}

	public static <T> String formatList(Iterable<T> values, Function<T, String> formatter) {
		return StreamEx.of(values.iterator()).map(formatter).collect(Collectors.joining("\n"));
	}

	public static String formatList(Iterable<String> values) {
		return StreamEx.of(values.iterator()).collect(Collectors.joining("\n"));
	}

	public static String formatVectorList(Iterable<bCVector> values) {
		return formatList(values, bCVector::toString);
	}

	public static String formatFloatList(Iterable<Float> values) {
		return formatList(values, Misc::formatFloat);
	}

	@SafeVarargs
	public static <T> List<T> asList(T... elements) {
		return Arrays.asList(elements);
	}

	public static <T> List<T> removeEntry(List<T> list, T entry) {
		list.removeIf(t -> t.equals(entry));
		return list;
	}

	public static String regexSearch(String regex, String source) {
		Matcher matcher = Pattern.compile(regex).matcher(source);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}

	public static float regexSearchFloat(String regex, String source, float standard) {
		String temp = regexSearch(regex, source);
		if (temp != null) {
			try {
				return Float.parseFloat(temp.replace(",", "."));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return standard;
	}

	public static float parseFloat(String s) {
		return Float.parseFloat(s.replace(",", "."));
	}

	public static <E> E elementAt(Iterable<E> iterable, int position) {
		Iterator<E> iterator = iterable.iterator();
		int curPosition = 0;
		while (iterator.hasNext()) {
			if (curPosition == position) {
				return iterator.next();
			}
			curPosition++;
		}
		return null;
	}

	public static boolean compareFloat(float float1, float float2, float epsilon) {
		return Math.abs(float1 - float2) < epsilon;
	}

	@SafeVarargs
	public static <T> T[] concat(T[] first, T... second) {
		T[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	/**
	 * Löscht alle Einträge, mit {@code start <= Eintrag < start + count}, und zieht von Einträgen,
	 * mit {@code Eintrag >= start + count}, {@code count} ab.
	 *
	 * @param list
	 * @param start
	 * @param count
	 */
	public static void removeRangeAndDisplace(List<Integer> list, int start, int count) {
		int removed = 0;
		for (int i = 0; i < list.size(); i++) {
			int index = list.get(i);
			if (index < start) {
				if (removed > 0) {
					list.set(i - removed, index);
				}
			} else if (index >= start + count) {
				list.set(i - removed, index - count);
			} else {
				removed++;
			}
		}

		for (int i = 0; i < removed; i++) {
			list.remove(list.size() - 1);
		}
	}

	public static String colorToHexStringRGB(Color color) {
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();

		int i = (r << 16) + (g << 8) + b;
		String s = Integer.toHexString(i).toUpperCase();
		while (s.length() < 6) {
			s = "0" + s;
		}
		return s;
	}

	public static <T, U, R> Iterable<R> zip(Iterable<T> ct, Iterable<U> cu, BiFunction<T, U, R> each) {
		return () -> new Iterator<R>() {
			private Iterator<T> it = ct.iterator();
			private Iterator<U> iu = cu.iterator();

			@Override
			public boolean hasNext() {
				return it.hasNext() && iu.hasNext();
			}

			@Override
			public R next() {
				return each.apply(it.next(), iu.next());
			}
		};
	}

	public static <R> R evaluate(Supplier<R> supplier) {
		return supplier.get();
	}
}
