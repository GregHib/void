package world.gregs.voidps.tools.inv.item;/* Class46 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class46 {
    int anInt698;
    int anInt709;
    int anInt789;
    Class46[] aClass46Array798;
    int anInt791;
    int anInt830;
    static byte[] aByteArray821 = new byte[32896];
    static Class196 aClass196_838;

    public static void method442(byte i) {
        if (i <= -8) {
            aClass196_838 = null;
            aByteArray821 = null;
        }
    }

    static {
        int i = 0;
        for (int i_69_ = 0; i_69_ < 256; i_69_++) {
            for (int i_70_ = 0; i_69_ >= i_70_; i_70_++)
                aByteArray821[i++] = (byte) (int) (255.0 / Math.sqrt((float) ((i_69_ * i_69_) + (i_70_ * i_70_) - -65535) / 65535.0F));
        }
        aClass196_838 = new Class196();
    }
}
