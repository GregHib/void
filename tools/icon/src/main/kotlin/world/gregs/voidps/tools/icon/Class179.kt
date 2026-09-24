package world.gregs.voidps.tools.icon;/* Class179 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class179 {
    static int anInt2361 = 1;
    static int anInt2363;

    static final Object wrap(byte[] is, boolean bool, byte i) {
        if (i < 73) anInt2361 = -51;
        anInt2363++;
        if (is == null) return null;
        if (is.length > 136 && !Class17.aBoolean247) {
            try {
                Class344 class344 = new Class344_Sub1();
                class344.method2691((byte) 62, is);
                return class344;
            } catch (Throwable throwable) {
                Class17.aBoolean247 = true;
            }
        }
        if (bool) return ha_Sub3.method3873(is, 0);
        return is;
    }
}
