package world.gregs.voidps.tools.inv.item;/* Class50_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class50_Sub1 {
    static int anInt5215;

    static final byte[] method461(boolean bool, Object object, int i) {
        anInt5215++;
        if (object == null) return null;
        if (object instanceof byte[]) {
            byte[] is = (byte[]) object;
            if (bool) return ha_Sub3.method3873(is, 0);
            return is;
        }
        if (i != 53146732) return null;
        if (object instanceof Class344) {
            Class344 class344 = (Class344) object;
            return class344.method2692(-3672);
        }
        throw new IllegalArgumentException();
    }
}
