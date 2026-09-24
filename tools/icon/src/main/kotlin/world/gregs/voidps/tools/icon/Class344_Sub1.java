package world.gregs.voidps.tools.icon;/* Class344_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import java.nio.ByteBuffer;

final class Class344_Sub1 extends Class344 {
    private ByteBuffer aByteBuffer6524;

    final byte[] method2692(int i) {
        byte[] is = new byte[aByteBuffer6524.capacity()];
        if (i != -3672) method2692(78);
        aByteBuffer6524.position(0);
        aByteBuffer6524.get(is);
        return is;
    }

    final void method2691(byte i, byte[] is) {
        aByteBuffer6524 = ByteBuffer.allocateDirect(is.length);
        aByteBuffer6524.position(0);
        if (i != 62) method2692(73);
        aByteBuffer6524.put(is);
    }
}
