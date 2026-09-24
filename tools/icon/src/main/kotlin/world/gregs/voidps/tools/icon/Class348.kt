package world.gregs.voidps.tools.icon;/* Class348 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

class Class348 {
    static int anInt4285;
    static Js5 aJs5_4286;
    Class348 aClass348_4294;
    Class348 aClass348_4295;
    static int anInt4297;
    long aLong4291;

    final boolean method2712(byte i) {
        if (i != 4) return true;
        anInt4297++;
        return this.aClass348_4295 != null;
    }

    final void unlink(byte i) {
        anInt4285++;
        if (this.aClass348_4295 != null) {
            this.aClass348_4295.aClass348_4294 = this.aClass348_4294;
            this.aClass348_4294.aClass348_4295 = this.aClass348_4295;
            if (i < 18) method2712((byte) 46);
            this.aClass348_4294 = null;
            this.aClass348_4295 = null;
        }
    }

    public Class348() {
        /* empty */
    }
}
