package world.gregs.voidps.tools.icon;/* Class348_Sub42_Sub8_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import java.lang.ref.SoftReference;

final class Class348_Sub42_Sub8_Sub1 extends Class348_Sub42_Sub8 {
    private final SoftReference aSoftReference10428;

    final boolean method3195(int i) {
        return true;
    }

    final Object method3193(int i) {
        if (i <= 75) return null;
        return aSoftReference10428.get();
    }

    Class348_Sub42_Sub8_Sub1(Object object, int i) {
        super(i);
        aSoftReference10428 = new SoftReference(object);
    }
}
