package world.gregs.voidps.tools.inv.item;/* Class230 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class230 {
    String aString2985;
    int anInt2987;
    static int anInt2989;

    public final String toString() {
        anInt2989++;
        throw new IllegalStateException();
    }

    Class230(String string, int i) {
        try {
            this.aString2985 = string;
            this.anInt2987 = i;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("sj.<init>(" + (string != null ? "{...}" : "null") + ',' + i + ')'));
        }
    }
}
