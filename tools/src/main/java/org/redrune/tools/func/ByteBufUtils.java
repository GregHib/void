package org.redrune.tools.func;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 24, 2020 4:46 p.m.
 */
public final class ByteBufUtils {

    public static ByteBuf rsa(ByteBuf buf, String modulus, String exponent) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        BigInteger cipherText = new BigInteger(bytes);
        BigInteger mod = new BigInteger(modulus);
        BigInteger exp = new BigInteger(exponent);
        BigInteger plainText = cipherText.modPow(exp, mod);

        return Unpooled.wrappedBuffer(plainText.toByteArray());
    }

    public static String readString(ByteBuf buffer) {
        buffer.markReaderIndex();

        int len = 0;
        while (buffer.readUnsignedByte() != 0)
            len++;

        buffer.resetReaderIndex();

        byte[] bytes = new byte[len];
        buffer.readBytes(bytes);
        buffer.readerIndex(buffer.readerIndex() + 1);
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    public static void writeString(ByteBuf buffer, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.ISO_8859_1);
        buffer.writeBytes(bytes);
        buffer.writeByte(0);
    }

    public static void writeSmart(ByteBuf buffer, int value) {
        if (value < 128)
            buffer.writeByte(value);
        else
            buffer.writeShort(32768 + value);
    }

    public static void writeWorldListString(ByteBuf buffer, String str) {
        buffer.writeByte(0);
        buffer.writeBytes(str.getBytes(StandardCharsets.ISO_8859_1));
        buffer.writeByte(0);
    }

    private ByteBufUtils() {

    }

}
