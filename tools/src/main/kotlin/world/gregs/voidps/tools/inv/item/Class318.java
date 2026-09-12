package world.gregs.voidps.tools.inv.item;/* Class318 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

class Class318 {
    Class318 aClass318_3970;
    static int anInt3975;
    Class318 aClass318_3976;

    final void method2373(boolean bool) {
        anInt3975++;
        if (this.aClass318_3976 != null) {
            this.aClass318_3976.aClass318_3970 = this.aClass318_3970;
            this.aClass318_3970.aClass318_3976 = this.aClass318_3976;
            this.aClass318_3970 = null;
            if (bool == false) this.aClass318_3976 = null;
        }
    }

    public Class318() {
        /* empty */
    }
}
