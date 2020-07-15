/*
 * Copyright (c) 2003-2008 jMonkeyEngine All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.george.g3utils.util;

import java.util.Random;

/**
 * <code>FastMath</code> provides 'fast' math approximations and float equivalents of Math
 * functions. These are all used as static values and functions.
 *
 * @author Various
 * @version $Id: FastMath.java,v 1.45 2007/08/26 08:44:20 irrisor Exp $
 */

final public class FastMath {

	private FastMath() {}

	/** A "close to zero" double epsilon value for use */
	public static final double DBL_EPSILON = 2.220446049250313E-16d;

	/** A "close to zero" float epsilon value for use */
	public static final float FLT_EPSILON = 1.1920928955078125E-7f;

	/** A "close to zero" float epsilon value for use */
	public static final float ZERO_TOLERANCE = 0.0001f;

	public static final float ONE_THIRD = 1f / 3f;

	/** The value PI as a float. (180 degrees) */
	public static final float PI = (float) Math.PI;

	/** The value 2PI as a float. (360 degrees) */
	public static final float TWO_PI = 2.0f * PI;

	/** The value PI/2 as a float. (90 degrees) */
	public static final float HALF_PI = 0.5f * PI;

	/** The value PI/4 as a float. (45 degrees) */
	public static final float QUARTER_PI = 0.25f * PI;

	/** The value 1/PI as a float. */
	public static final float INV_PI = 1.0f / PI;

	/** The value 1/(2PI) as a float. */
	public static final float INV_TWO_PI = 1.0f / TWO_PI;

	/** A value to multiply a degree value by, to convert it to radians. */
	public static final float DEG_TO_RAD = PI / 180.0f;

	/** A value to multiply a radian value by, to convert it to degrees. */
	public static final float RAD_TO_DEG = 180.0f / PI;

	/** A precreated random object for random numbers. */
	public static final Random rand = new Random();

	/**
	 * Returns true if the number is a power of 2 (2,4,8,16...) A good implementation found on the
	 * Java boards. note: a number is a power of two if and only if it is the smallest number with
	 * that number of significant bits. Therefore, if you subtract 1, you know that the new number
	 * will have fewer bits, so ANDing the original number with anything less than it will give 0.
	 *
	 * @param number The number to test.
	 * @return True if it is a power of two.
	 */
	public static boolean isPowerOfTwo(int number) {
		return number > 0 && (number & number - 1) == 0;
	}

	public static int nearestPowerOfTwo(int number) {
		return (int) Math.pow(2, Math.ceil(Math.log(number) / Math.log(2)));
	}

	/**
	 * Linear interpolation from startValue to endValue by the given percent. Basically: ((1 -
	 * percent) * startValue) + (percent * endValue)
	 *
	 * @param scale scale value to use. if 1, use endValue, if 0, use startValue.
	 * @param startValue Begining value. 0% of f
	 * @param endValue ending value. 100% of f
	 * @return The interpolated value between startValue and endValue.
	 */
	public static float interpolateLinear(float scale, float startValue, float endValue) {
		if (startValue == endValue) {
			return startValue;
		}
		if (scale <= 0f) {
			return startValue;
		}
		if (scale >= 1f) {
			return endValue;
		}
		return (1f - scale) * startValue + scale * endValue;
	}

	/**
	 * Returns the arc cosine of an angle given in radians.<br>
	 * Special cases:
	 * <ul>
	 * <li>If fValue is smaller than -1, then the result is PI.
	 * <li>If the argument is greater than 1, then the result is 0.
	 * </ul>
	 *
	 * @param fValue The angle, in radians.
	 * @return fValue's acos
	 * @see java.lang.Math#acos(double)
	 */
	public static float acos(float fValue) {
		if (-1.0f < fValue) {
			if (fValue < 1.0f) {
				return (float) Math.acos(fValue);
			}

			return 0.0f;
		}

		return PI;
	}

	/**
	 * Returns the arc sine of an angle given in radians.<br>
	 * Special cases:
	 * <ul>
	 * <li>If fValue is smaller than -1, then the result is -HALF_PI.
	 * <li>If the argument is greater than 1, then the result is HALF_PI.
	 * </ul>
	 *
	 * @param fValue The angle, in radians.
	 * @return fValue's asin
	 * @see java.lang.Math#asin(double)
	 */
	public static float asin(float fValue) {
		if (-1.0f < fValue) {
			if (fValue < 1.0f) {
				return (float) Math.asin(fValue);
			}

			return HALF_PI;
		}

		return -HALF_PI;
	}

	/**
	 * Returns the arc tangent of an angle given in radians.<br>
	 *
	 * @param fValue The angle, in radians.
	 * @return fValue's asin
	 * @see java.lang.Math#atan(double)
	 */
	public static float atan(float fValue) {
		return (float) Math.atan(fValue);
	}

	/**
	 * A direct call to Math.atan2.
	 *
	 * @param fY
	 * @param fX
	 * @return Math.atan2(fY,fX)
	 * @see java.lang.Math#atan2(double, double)
	 */
	public static float atan2(float fY, float fX) {
		return (float) Math.atan2(fY, fX);
	}

	/**
	 * Rounds a fValue up. A call to Math.ceil
	 *
	 * @param fValue The value.
	 * @return The fValue rounded up
	 * @see java.lang.Math#ceil(double)
	 */
	public static float ceil(float fValue) {
		return (float) Math.ceil(fValue);
	}

	/**
	 * Fast Trig functions for x86. This forces the trig functiosn to stay within the safe area on
	 * the x86 processor (-45 degrees to +45 degrees) The results may be very slightly off from what
	 * the Math and StrictMath trig functions give due to rounding in the angle reduction but it
	 * will be very very close. note: code from wiki posting on java.net by jeffpk
	 */
	public static float reduceSinAngle(float radians) {
		radians %= TWO_PI; // put us in -2PI to +2PI space
		if (Math.abs(radians) > PI) {
			radians = radians - TWO_PI;
		}
		if (Math.abs(radians) > HALF_PI) {
			radians = PI - radians;
		}

		return radians;
	}

	/**
	 * Returns sine of a value. note: code from wiki posting on java.net by jeffpk
	 *
	 * @param fValue The value to sine, in radians.
	 * @return The sine of fValue.
	 * @see java.lang.Math#sin(double)
	 */
	public static float sin2(float fValue) {
		fValue = reduceSinAngle(fValue); // limits angle to between -PI/2 and +PI/2
		if (Math.abs(fValue) <= Math.PI / 4) {
			return (float) Math.sin(fValue);
		}

		return (float) Math.cos(Math.PI / 2 - fValue);
	}

	/**
	 * Returns cos of a value.
	 *
	 * @param fValue The value to cosine, in radians.
	 * @return The cosine of fValue.
	 * @see java.lang.Math#cos(double)
	 */
	public static float cos2(float fValue) {
		return sin2(fValue + HALF_PI);
	}

	public static float cos(float v) {
		return (float) Math.cos(v);
	}

	public static float sin(float v) {
		return (float) Math.sin(v);
	}

	/**
	 * Returns E^fValue
	 *
	 * @param fValue Value to raise to a power.
	 * @return The value E^fValue
	 * @see java.lang.Math#exp(double)
	 */
	public static float exp(float fValue) {
		return (float) Math.exp(fValue);
	}

	/**
	 * Returns Absolute value of a float.
	 *
	 * @param fValue The value to abs.
	 * @return The abs of the value.
	 * @see java.lang.Math#abs(float)
	 */
	public static float abs(float fValue) {
		if (fValue < 0) {
			return -fValue;
		}
		return fValue;
	}

	/**
	 * Returns a number rounded down.
	 *
	 * @param fValue The value to round
	 * @return The given number rounded down
	 * @see java.lang.Math#floor(double)
	 */
	public static float floor(float fValue) {
		return (float) Math.floor(fValue);
	}

	/**
	 * Returns 1/sqrt(fValue)
	 *
	 * @param fValue The value to process.
	 * @return 1/sqrt(fValue)
	 * @see java.lang.Math#sqrt(double)
	 */
	public static float invSqrt(float fValue) {
		return (float) (1.0f / Math.sqrt(fValue));
	}

	public static float fastInvSqrt(float x) {
		float xhalf = 0.5f * x;
		int i = Float.floatToIntBits(x); // get bits for floating value
		i = 0x5f375a86 - (i >> 1); // gives initial guess y0
		x = Float.intBitsToFloat(i); // convert bits back to float
		x = x * (1.5f - xhalf * x * x); // Newton step, repeating increases accuracy
		return x;
	}

	/**
	 * Returns the log base E of a value.
	 *
	 * @param fValue The value to log.
	 * @return The log of fValue base E
	 * @see java.lang.Math#log(double)
	 */
	public static float log(float fValue) {
		return (float) Math.log(fValue);
	}

	/**
	 * Returns the logarithm of value with given base, calculated as log(value)/log(base), so that
	 * pow(base, return)==value (contributed by vear)
	 *
	 * @param value The value to log.
	 * @param base Base of logarithm.
	 * @return The logarithm of value with given base
	 */
	public static float log(float value, float base) {
		return (float) (Math.log(value) / Math.log(base));
	}

	/**
	 * Returns a number raised to an exponent power. fBase^fExponent
	 *
	 * @param fBase The base value (IE 2)
	 * @param fExponent The exponent value (IE 3)
	 * @return base raised to exponent (IE 8)
	 * @see java.lang.Math#pow(double, double)
	 */
	public static float pow(float fBase, float fExponent) {
		return (float) Math.pow(fBase, fExponent);
	}

	/**
	 * Returns the value squared. fValue ^ 2
	 *
	 * @param fValue The vaule to square.
	 * @return The square of the given value.
	 */
	public static float sqr(float fValue) {
		return fValue * fValue;
	}

	/**
	 * Returns the square root of a given value.
	 *
	 * @param fValue The value to sqrt.
	 * @return The square root of the given value.
	 * @see java.lang.Math#sqrt(double)
	 */
	public static float sqrt(float fValue) {
		return (float) Math.sqrt(fValue);
	}

	/**
	 * Returns the tangent of a value. If USE_FAST_TRIG is enabled, an approximate value is
	 * returned. Otherwise, a direct value is used.
	 *
	 * @param fValue The value to tangent, in radians.
	 * @return The tangent of fValue.
	 * @see java.lang.Math#tan(double)
	 */
	public static float tan(float fValue) {
		return (float) Math.tan(fValue);
	}

	/**
	 * Returns 1 if the number is positive, -1 if the number is negative, and 0 otherwise
	 *
	 * @param iValue The integer to examine.
	 * @return The integer's sign.
	 */
	public static int sign(int iValue) {
		if (iValue > 0) {
			return 1;
		}
		if (iValue < 0) {
			return -1;
		}
		return 0;
	}

	/**
	 * Returns 1 if the number is positive, -1 if the number is negative, and 0 otherwise
	 *
	 * @param fValue The float to examine.
	 * @return The float's sign.
	 */
	public static float sign(float fValue) {
		return Math.signum(fValue);
	}

	/**
	 * Returns the determinant of a 4x4 matrix.
	 */
	public static float determinant(double m00, double m01, double m02, double m03, double m10, double m11, double m12, double m13,
			double m20, double m21, double m22, double m23, double m30, double m31, double m32, double m33) {

		double det01 = m20 * m31 - m21 * m30;
		double det02 = m20 * m32 - m22 * m30;
		double det03 = m20 * m33 - m23 * m30;
		double det12 = m21 * m32 - m22 * m31;
		double det13 = m21 * m33 - m23 * m31;
		double det23 = m22 * m33 - m23 * m32;
		return (float) (m00 * (m11 * det23 - m12 * det13 + m13 * det12) - m01 * (m10 * det23 - m12 * det03 + m13 * det02)
				+ m02 * (m10 * det13 - m11 * det03 + m13 * det01) - m03 * (m10 * det12 - m11 * det02 + m12 * det01));
	}

	/**
	 * Returns a random float between 0 and 1.
	 *
	 * @return A random float between <tt>0.0f</tt> (inclusive) to <tt>1.0f</tt> (exclusive).
	 */
	public static float nextRandomFloat() {
		return rand.nextFloat();
	}

	/**
	 * Returns a random float between min and max.
	 *
	 * @return A random int between <tt>min</tt> (inclusive) to <tt>max</tt> (inclusive).
	 */
	public static int nextRandomInt(int min, int max) {
		return (int) (nextRandomFloat() * (max - min + 1)) + min;
	}

	public static int nextRandomInt() {
		return rand.nextInt();
	}

	/**
	 * Takes an value and expresses it in terms of min to max.
	 *
	 * @param val - the angle to normalize (in radians)
	 * @return the normalized angle (also in radians)
	 */
	public static float normalize(float val, float min, float max) {
		if (Float.isInfinite(val) || Float.isNaN(val)) {
			return 0f;
		}
		float range = max - min;
		while (val > max) {
			val -= range;
		}
		while (val < min) {
			val += range;
		}
		return val;
	}

	/**
	 * @param x the value whose sign is to be adjusted.
	 * @param y the value whose sign is to be used.
	 * @return x with its sign changed to match the sign of y.
	 */
	public static float copysign(float x, float y) {
		if (y >= 0 && x <= -0) {
			return -x;
		} else if (y < 0 && x >= 0) {
			return -x;
		} else {
			return x;
		}
	}

	/**
	 * Take a float input and clamp it between min and max.
	 *
	 * @param input
	 * @param min
	 * @param max
	 * @return clamped input
	 */
	public static float clamp(float input, float min, float max) {
		return input < min ? min : input > max ? max : input;
	}

	/**
	 * Clamps the given float to be between 0 and 1.
	 *
	 * @param input
	 * @return input clamped between 0 and 1.
	 */
	public static float saturate(float input) {
		return clamp(input, 0f, 1f);
	}

	/**
	 * Converts a single precision (32 bit) floating point value into half precision (16 bit).
	 * Source: http://www.fox-toolkit.org/ftp/fasthalffloatconversion.pdf
	 *
	 * @param half The half floating point value as a short.
	 * @return floating point value of the half.
	 */
	public static float convertHalfToFloat(short half) {
		switch (half) {
			case 0x0000:
				return 0f;
			case (short) 0x8000:
				return -0f;
			case 0x7c00:
				return Float.POSITIVE_INFINITY;
			case (short) 0xfc00:
				return Float.NEGATIVE_INFINITY;
			// TODO: Support for NaN?
			default:
				return Float.intBitsToFloat((half & 0x8000) << 16 | (half & 0x7c00) + 0x1C000 << 13 | (half & 0x03FF) << 13);
		}
	}

	public static short convertFloatToHalf(float flt) {
		if (Float.isNaN(flt)) {
			throw new UnsupportedOperationException("NaN to half conversion not supported!");
		} else if (flt == Float.POSITIVE_INFINITY) {
			return (short) 0x7c00;
		} else if (flt == Float.NEGATIVE_INFINITY) {
			return (short) 0xfc00;
		} else if (flt == 0f) {
			return (short) 0x0000;
		} else if (flt == -0f) {
			return (short) 0x8000;
		} else if (flt > 65504f) {
			// max value supported by half float
			return 0x7bff;
		} else if (flt < -65504f) {
			return (short) (0x7bff | 0x8000);
		} else if (flt > 0f && flt < 5.96046E-8f) {
			return 0x0001;
		} else if (flt < 0f && flt > -5.96046E-8f) {
			return (short) 0x8001;
		}

		int f = Float.floatToIntBits(flt);
		return (short) (f >> 16 & 0x8000 | (f & 0x7f800000) - 0x38000000 >> 13 & 0x7c00 | f >> 13 & 0x03ff);
	}

}
