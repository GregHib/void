package world.gregs.voidps.tools.inv.item;/* Class348_Sub37 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub37 {
    static int anInt6996;

    static final Class348_Sub40 method3031(int i, Class348_Sub49 class348_sub49) {
        anInt6996++;
        class348_sub49.readUnsignedByte(255);
        int i_0_ = class348_sub49.readUnsignedByte(255);
        Class348_Sub40 class348_sub40 = Class59_Sub1_Sub1.method557(i_0_, (byte) -84);
        class348_sub40.anInt7036 = class348_sub49.readUnsignedByte(255);
        int i_1_ = class348_sub49.readUnsignedByte(255);
        if (i < 123) return null;
        for (int i_2_ = 0; i_1_ > i_2_; i_2_++) {
            int i_3_ = class348_sub49.readUnsignedByte(255);
            class348_sub40.method3049(class348_sub49, i_3_, 31015);
        }
        class348_sub40.method3044(120);
        return class348_sub40;
    }
}
