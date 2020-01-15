package util.buffer;

/**
 * Created at: Nov 27, 2016 8:48:52 PM
 *
 * @author Walied-Yassen A.k.A Cody
 */
public class FixedBuffer extends Buffer {

	/**
	 * Constructs a new {@link FixedBuffer} object instance.
	 *
	 * @param data the buffer data.
	 */
	public FixedBuffer(byte[] data) {
		super(data);
	}

	/**
	 * Constructs a new {@link FixedBuffer} object instance.
	 *
	 * @param size the buffer size.
	 */
	public FixedBuffer(int size) {
		super(new byte[size]);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.rs2.server.core.buffer.Buffer#readByte()
	 */
	@Override
	public int readByte(int offset) {
		if (offset >= data.length) {
			throw new ArrayIndexOutOfBoundsException("Can't read byte at offset:" + offset + ", Exceeds the buffer limit!");
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
			throw new IllegalArgumentException("Can't write at offset: " + offset + ", Outside the buffer bounds!");
		}
		data[offset] = (byte) value;
	}

}
