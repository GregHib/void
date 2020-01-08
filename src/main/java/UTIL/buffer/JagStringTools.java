package UTIL.buffer;

/**
 * Created at: Nov 27, 2016 8:40:12 PM
 *
 * @author Walied-Yassen A.k.A Cody
 */
public class JagStringTools {

	/**
	 * The special characters list.
	 */
	private static final char[] CHARACTERS = {'\u20ac', '\0', '\u201a', '\u0192', '\u201e', '\u2026', '\u2020', '\u2021', '\u02c6', '\u2030', '\u0160', '\u2039', '\u0152', '\0', '\u017d', '\0', '\0', '\u2018', '\u2019', '\u201c', '\u201d', '\u2022', '\u2013', '\u2014', '\u02dc', '\u2122', '\u0161', '\u203a', '\u0153', '\0', '\u017e', '\u0178'};

	/**
	 * Decodes the Jagex string down from a buffer.
	 *
	 * @param buffer the buffer to decode from.
	 * @param start  the decode start offset.
	 * @param length the decode end offset.
	 * @return the decoded {@link String} value.
	 */
	public static String decode(byte[] buffer, int start, int length) {
		char[] chs = new char[length];
		int bufferIndex = 0;
		for (int localIndex = 0; localIndex < length; localIndex++) {
			int hc = buffer[start + localIndex] & 0xff;
			if (hc != 0) {
				if (hc >= 128 && hc < 160) {
					int escaped = CHARACTERS[hc - 128];
					if (0 == escaped) {
						escaped = 63;
					}
					hc = escaped;
				}
			}
			chs[bufferIndex++] = (char) hc;

		}
		return new String(chs, 0, bufferIndex);
	}

	/**
	 * Decodes the given character to a Jagex type character.
	 *
	 * @param input the character to decode.
	 * @return the decoded character as Jagex special type.
	 */
	public static char decode(byte input) {
		int output = input & 0xff;
		if (output == 0) {
			throw new IllegalArgumentException("Could not parse JagString character of: " + Integer.toString(output, 16));
		}
		if (output >= 128 && output < 160) {
			int escaped = CHARACTERS[output - 128];
			if (escaped == 0) {
				escaped = 63;
			}
			output = escaped;
		}
		return (char) output;
	}

	/**
	 * Encodes the specified Jagex string value into the buffer.
	 *
	 * @param text         the Jagex string value to encode.
	 * @param start        the encode start offset.
	 * @param end          the encode end offset.
	 * @param buffer       the buffer to encode to.
	 * @param bufferOffset the buffer write offset.
	 * @return the encoded characters count.
	 */
	public static int encode(String text, int start, int end, byte[] buffer, int bufferOffset) {
		int length = end - start;
		for (int index = 0; index < length; index++) {
			char ch = text.charAt(index + start);
			if (ch > 0 && ch < '\u0080' || ch >= '\u00a0' && ch <= '\u00ff') {
				buffer[bufferOffset + index] = (byte) ch;
			} else if ('\u20ac' == ch) {
				buffer[bufferOffset + index] = (byte) -128;
			} else if (ch == '\u201a') {
				buffer[bufferOffset + index] = (byte) -126;
			} else if ('\u0192' == ch) {
				buffer[bufferOffset + index] = (byte) -125;
			} else if (ch == '\u201e') {
				buffer[bufferOffset + index] = (byte) -124;
			} else if ('\u2026' == ch) {
				buffer[bufferOffset + index] = (byte) -123;
			} else if (ch == '\u2020') {
				buffer[bufferOffset + index] = (byte) -122;
			} else if ('\u2021' == ch) {
				buffer[bufferOffset + index] = (byte) -121;
			} else if (ch == '\u02c6') {
				buffer[bufferOffset + index] = (byte) -120;
			} else if (ch == '\u2030') {
				buffer[bufferOffset + index] = (byte) -119;
			} else if (ch == '\u0160') {
				buffer[bufferOffset + index] = (byte) -118;
			} else if (ch == '\u2039') {
				buffer[bufferOffset + index] = (byte) -117;
			} else if (ch == '\u0152') {
				buffer[bufferOffset + index] = (byte) -116;
			} else if ('\u017d' == ch) {
				buffer[bufferOffset + index] = (byte) -114;
			} else if (ch == '\u2018') {
				buffer[bufferOffset + index] = (byte) -111;
			} else if (ch == '\u2019') {
				buffer[bufferOffset + index] = (byte) -110;
			} else if ('\u201c' == ch) {
				buffer[bufferOffset + index] = (byte) -109;
			} else if (ch == '\u201d') {
				buffer[bufferOffset + index] = (byte) -108;
			} else if ('\u2022' == ch) {
				buffer[bufferOffset + index] = (byte) -107;
			} else if (ch == '\u2013') {
				buffer[bufferOffset + index] = (byte) -106;
			} else if (ch == '\u2014') {
				buffer[bufferOffset + index] = (byte) -105;
			} else if (ch == '\u02dc') {
				buffer[bufferOffset + index] = (byte) -104;
			} else if (ch == '\u2122') {
				buffer[bufferOffset + index] = (byte) -103;
			} else if (ch == '\u0161') {
				buffer[bufferOffset + index] = (byte) -102;
			} else if ('\u203a' == ch) {
				buffer[bufferOffset + index] = (byte) -101;
			} else if ('\u0153' == ch) {
				buffer[bufferOffset + index] = (byte) -100;
			} else if ('\u017e' == ch) {
				buffer[bufferOffset + index] = (byte) -98;
			} else if ('\u0178' == ch) {
				buffer[bufferOffset + index] = (byte) -97;
			} else {
				buffer[bufferOffset + index] = (byte) 63;
			}
		}
		return length;
	}

}
