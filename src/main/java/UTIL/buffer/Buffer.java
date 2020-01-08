package UTIL.buffer;

import java.util.Arrays;

/**
 * Created at: Nov 26, 2016 6:47:16 PM
 *
 * @author Walied-Yassen A.k.A Cody
 */
public abstract class Buffer {

	/**
	 * The buffer data.
	 */
	protected byte[] data;

	/**
	 * The buffer offset.
	 */
	protected int offset;

	/**
	 * Constructs a new {@link Buffer} object instance.
	 *
	 * @param data the initial buffer data.
	 */
	public Buffer(byte[] data) {
		this.data = data;
	}

	/**
	 * Gets the remaining amount of data within the buffer
	 */
	public int remaining() {
		return data.length - offset;
	}

	/**
	 * Writes the specified {@code short} value type to the buffer.
	 *
	 * @param value the value to write, ranges in {@code short} space.
	 */
	public void writeShort(int value) {
		writeByte(value >> 8);
		writeByte(value);
	}

	/**
	 * Reads the next signed {@code short} value.
	 *
	 * @return the read short value.
	 */
	public int readShort() {
		int value = readUnsignedShort();
		if (value > Short.MAX_VALUE) {
			value -= 0x10000;
		}
		return value;
	}

	/**
	 * Reads the next unsigned {@like short} value.
	 *
	 * @return the read short value.
	 */
	public int readUnsignedShort() {
		return (readUnsignedByte() << 8) + readUnsignedByte();
	}

	/**
	 * Reads the next unsigned byte from the buffer.
	 *
	 * @return the unsigned byte that was read.
	 */
	public int readUnsignedByte() {
		return readByte() & 0xff;
	}

	/**
	 * Reads the next signed byte from the buffer.
	 *
	 * @return the signed byte that was read.
	 */
	public int readByte() {
		return readByte(offset++);
	}

	/**
	 * Reads the byte at the specified offset.
	 *
	 * @param offset the offset which we are trying to retrieve it's value.
	 * @return the data which is located at the specified offset.
	 */
	public abstract int readByte(int offset);

	/**
	 * Writes the specified triple byte value to the buffer.
	 *
	 * @param value the value to write.
	 */
	public void writeTriByte(int value) {
		writeByte(value >> 16);
		writeByte(value >> 8);
		writeByte(value);
	}

	/**
	 * Writes the specified value as {@code byte} to the next slot.
	 *
	 * @param value the value of the byte to write.
	 */
	public void writeByte(int value) {
		writeByte(offset++, value);
	}

	/**
	 * Writes the specified value at the specified offset.
	 *
	 * @param offset the offset to write at.
	 * @param value  the value to write.
	 */
	public abstract void writeByte(int offset, int value);

	/**
	 * Reads the next signed triple byte value.
	 *
	 * @return the read triple byte value.
	 */
	public int readTriByte() {
		int value = readUnsignedShort();
		if (value > 0x7fffff) {
			value -= 0x1000000;
		}
		return value;
	}

	/**
	 * Reads the next unsigned triple byte value.
	 *
	 * @return the read short value.
	 */
	public int readUnsignedTriByte() {
		return (readUnsignedByte() << 16) + (readUnsignedByte() << 8) + readUnsignedByte();
	}

	/**
	 * Writes the specified long value to the buffer.
	 *
	 * @param value the long value to write.
	 */
	public void writeLong(long value) {
		writeByte((int) (value >> 56));
		writeByte((int) (value >> 48));
		writeByte((int) (value >> 40));
		writeByte((int) (value >> 32));
		writeByte((int) (value >> 24));
		writeByte((int) (value >> 16));
		writeByte((int) (value >> 8));
		writeByte((int) value);
	}

	/**
	 * Reads the next long value form the buffer.
	 *
	 * @return the read long value.
	 */
	public long readLong() {
		long right = readInt() & 0xffffffffL;
		long left = (readInt() & 0xffffffffL) << 32;
		return right + left;
	}

	/**
	 * Reads the next integer value form the buffer.
	 *
	 * @return the read integer value.
	 */
	public int readInt() {
		return (readUnsignedByte() << 24) + (readUnsignedByte() << 16) + (readUnsignedByte() << 8) + readUnsignedByte();
	}

	/**
	 * Writes a string literal to the buffer.
	 *
	 * @param value the string literal to write.
	 */
	public void writeString(String value) {
		int fakeEnd = value.indexOf('\0');
		if (fakeEnd >= 0) {
			throw new IllegalArgumentException("Unexpected end of jagstring '\\0'!");
		}
		byte[] encoded = new byte[value.length()];
		JagStringTools.encode(value, 0, value.length(), encoded, 0);
		write(encoded);
		writeByte(0);

	}

	/**
	 * Reads a jagex type string literal from the buffer.
	 *
	 * @return the read string value.
	 */
	public String readJagString() {
		if (readByte() != 0) {
			throw new RuntimeException("Unformatted jagex string!");
		}
		return readString();
	}

	/**
	 * Reads a string literal from the buffer.
	 *
	 * @return the read string value.
	 */
	public String readString() {
		int start = offset;
		while (readByte() != 0) {
		}
		int length = offset - start - 1;
		if (length == 0) {
			return "";
		}
		return JagStringTools.decode(data, start, length);
	}

	/**
	 * Reads a versioned string
	 */
	public String readVString() {
		if (readByte() != 0) {
			throw new RuntimeException("GJSTR2 - bad magic number");
		}
		return readString();
	}

	/**
	 * Writes a jagex type string literal to the buffer.
	 *
	 * @param value the string literal to write.
	 */
	public void writeJagString(String value) {
		writeByte(0);
		writeString(value);
	}

	/**
	 * Reads a boolean value from the buffer.
	 *
	 * @return the read {@code boolean} value.
	 */
	public boolean readBoolean() {
		return readUnsignedByte() == 1;
	}

	/**
	 * Writes a boolean value to the buffer.
	 *
	 * @param value the {@code boolean} value to write.
	 */
	public void writeBoolean(boolean value) {
		writeByte(value ? 1 : 0);
	}

	/**
	 * Reads a smart byte-short range value.
	 *
	 * @return the read value.
	 */
	public int readByteSmart() {
		int value = data[offset] & 0xff;
		if (value < 128) {
			return readUnsignedByte();
		}
		return readUnsignedShort() - 32768;
	}

	/**
	 * Writes a smart byte-short range value.
	 *
	 * @param value the value to write.
	 */
	public void writeByteSmart(int value) {
		if (value >= 128) {
			writeShort(value + 32768);
		} else {
			writeByte(value);
		}
	}

	/**
	 * Reads a smart short-integer range value.
	 *
	 * @return the read value.
	 */
	public int readShortSmart() {
		if (data[offset] < 0) {
			return readInt() & 0x7fffffff;
		}
		return readUnsignedShort();
	}

	/**
	 * Reads a smart short-integer range value with null support.
	 *
	 * @return the read value.
	 */
	public int readShortSmartNS() {
		int value = readShortSmart();
		if (value == Short.MAX_VALUE) {
			return -1;
		}
		return value;
	}

	/**
	 * Reads a huge incremental smart from the buffer.
	 *
	 * @return the read smart value.
	 */
	public int readHugeSmart() {
		int value = 0;
		int incr;
		for (incr = readByteSmart(); incr == 32767; incr = readByteSmart()) {
			value += 32767;
		}
		value += incr;
		return value;
	}

	/**
	 * Writes the specified data to the buffer.
	 *
	 * @param data the data to write.
	 */
	public void write(byte[] data) {
		write(data, 0, data.length);
	}

	/**
	 * Writes the specified data to the buffer, starting from the specified offset.
	 *
	 * @param data the data to write.
	 * @param off  the data write start offset.
	 * @param len  the data write end offset.
	 */
	public void write(byte[] data, int off, int len) {
		for (; off < len; off++) {
			writeByte(data[off]);
		}
	}

	/**
	 * Decodes the buffer using XTEA algorithm.
	 *
	 * @param keys  the XTEA keys.
	 * @param start the decode start offset.
	 * @param end   the decode end offset.
	 */
	public void decodeXTEA(int keys[], int start, int end) {
		int startOffset = offset;
		offset = start;
		int i1 = (end - start) / 8;
		for (int j1 = 0; j1 < i1; j1++) {
			int k1 = readInt();
			int l1 = readInt();
			int sum = 0xc6ef3720;
			int delta = 0x9e3779b9;
			for (int k2 = 32; k2-- > 0; ) {
				l1 -= keys[(sum & 0x1c84) >>> 11] + sum ^ (k1 >>> 5 ^ k1 << 4) + k1;
				sum -= delta;
				k1 -= (l1 >>> 5 ^ l1 << 4) + l1 ^ keys[sum & 3] + sum;
			}
			offset -= 8;
			writeInt(k1);
			writeInt(l1);
		}
		offset = startOffset;
	}

	/**
	 * Writes the specified integer value to the buffer.
	 *
	 * @param value the integer value to write.
	 */
	public void writeInt(int value) {
		writeByte(value >> 24);
		writeByte(value >> 16);
		writeByte(value >> 8);
		writeByte(value);
	}

	/**
	 * Encodes the buffer using XTEA algorithm.
	 *
	 * @param keys  the XTEA keys.
	 * @param start the encode start offset.
	 * @param end   the encode end offset.
	 */
	public final void encodeXTEA(int keys[], int start, int end) {
		int o = offset;
		int j = (end - start) / 8;
		offset = start;
		for (int k = 0; k < j; k++) {
			int l = readInt();
			int i1 = readInt();
			int sum = 0;
			int delta = 0x9e3779b9;
			for (int l1 = 32; l1-- > 0; ) {
				l += sum + keys[3 & sum] ^ i1 + (i1 >>> 5 ^ i1 << 4);
				sum += delta;
				i1 += l + (l >>> 5 ^ l << 4) ^ keys[(0x1eec & sum) >>> 11] + sum;
			}

			offset -= 8;
			writeInt(l);
			writeInt(i1);
		}
		offset = o;
	}

	/**
	 * Skips a specified amount of bytes and return the skip-before offset.
	 *
	 * @param length the amount of bytes to skip.
	 * @return the skip-before offset.
	 */
	public int skipBefore(int length) {
		int offset = this.offset;
		read(new byte[length]);
		return offset;
	}

	/**
	 * Fills the given byte array with a read data from the buffer.
	 *
	 * @param data the data to fill.
	 * @return the read data.
	 */
	public byte[] read(byte[] data) {
		return read(data, 0, data.length);
	}

	/**
	 * Reads a specified amount of data into a specified {@code byte[]} byte array..
	 *
	 * @param data the byte array to store the read data at.
	 * @param off  the data read start offset.
	 * @param len  the data read end offset.
	 * @return the read data.
	 */
	public byte[] read(byte[] data, int off, int len) {
		for (; off < len; off++) {
			data[off] = (byte) readByte();
		}
		return data;
	}

	/**
	 * Skips a specified amount of bytes and return the skip-after offset.
	 *
	 * @param length the amount of bytes to skip.
	 * @return the skip-after offset.
	 */
	public int skipAfter(int length) {
		read(new byte[length]);
		return offset;
	}

	/**
	 * Gets the available bytes count.
	 *
	 * @return the available bytes count.
	 */
	public int getRemaining() {
		return data.length - offset;
	}

	/**
	 * Gets the current data.
	 *
	 * @return the current data.
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Gets the data trimmed.
	 *
	 * @return the trimmed data.
	 */
	public byte[] getDataTrimmed() {
		return Arrays.copyOf(data, offset);
	}

	/**
	 * Gets the current offset.
	 *
	 * @return the current offset.
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Sets the buffer offset.
	 *
	 * @param offset the new offset.
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * Gets the data length.
	 *
	 * @return the data length.
	 */
	public int getLength() {
		return data.length;
	}

	public int read24BitInt() {
		return (readUnsignedByte() << 16) + (readUnsignedByte() << 8) + (readUnsignedByte());
	}
}
