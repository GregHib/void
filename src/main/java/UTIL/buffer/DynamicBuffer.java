package UTIL.buffer;

/**
 * Created at: Nov 27, 2016 8:47:42 PM
 *
 * @author Walied-Yassen A.k.A Cody
 */
public class DynamicBuffer extends Buffer {

	/**
	 * The buffer extending rate.
	 */
	private static final int EXTEND_RATE = 64;

	/**
	 * Constructs a new {@link DynamicBuffer} object instance.
	 *
	 * @param data the initial buffer data.
	 */
	public DynamicBuffer(byte[] data) {
		super(data);
	}

	/**
	 * Constructs a new {@link DynamicBuffer} object instance.
	 *
	 * @param size the initial buffer size.
	 */
	public DynamicBuffer(int size) {
		super(new byte[size]);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.rs2.server.core.buffer.Buffer#readByte(int)
	 */
	@Override
	public int readByte(int offset) {
		if (offset >= data.length) {
			return 0;
		}
		return data[offset];
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.rs2.server.core.buffer.Buffer#writeByte(int, int)
	 */
	@Override
	public void writeByte(int offset, int value) {
		if (offset >= data.length) {
			extend(offset - data.length + EXTEND_RATE);
		}
		data[offset] = (byte) value;
	}

	/**
	 * Extends the buffer by a specified length.
	 *
	 * @param length the length to extend by.
	 */
	protected void extend(int length) {
		byte[] newData = new byte[data.length + length];
		System.arraycopy(data, 0, newData, 0, data.length);
		data = newData;
	}

	/**
	 * Checks if we would need an extend at the specified offset.
	 *
	 * @param offset the offset to check at.
	 */
	public void checkExtend(int offset) {
		if (offset >= data.length) {
			extend(offset + 1 - data.length);
		}
	}

}
