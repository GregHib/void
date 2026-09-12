package world.gregs.voidps.tools.inv.item;/* Class348_Sub40_Sub15 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub40_Sub15 extends Class348_Sub40 {
    static Class114 aClass114_9216 = new Class114(91, 2);
    static int anInt9217;
    private int anInt9220 = 4096;
    static int anInt9221;

    final void method3049(Packet packet, int i, int i_0_) {
        int i_1_ = i;
        if (i_1_ == 0) anInt9220 = (packet.readUnsignedByte(255) << 12) / 255;
        if (i_0_ == 31015) anInt9217++;
    }

    public static void method3085(int i) {
        aClass114_9216 = null;
        if (i != 0) aClass114_9216 = null;
    }

    Class348_Sub40_Sub15(int i) {
        super(0, true);
        anInt9220 = i;
    }

    final int[] method3042(int i, int i_8_) {
        anInt9221++;
        int[] is = this.aClass191_7032.method1433(0, i);
        if (this.aClass191_7032.aBoolean2570) Class214.method1579(is, 0, Class348_Sub40_Sub6.anInt9139, anInt9220);
        if (i_8_ != 255) method3085(63);
        return is;
    }

    public Class348_Sub40_Sub15() {
        this(4096);
    }
}
