package world.gregs.voidps.tools.inv.item;/* Class348_Sub16_Sub2 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class348_Sub16_Sub2 {
    static int anInt8882;

    static final void method2832(int[] is, long[] ls, int i) {
        try {
            IOException_Sub1.method129(i, i + -107, ls, ls.length - 1, is);
            anInt8882++;
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("iha.I(" + (is != null ? "{...}" : "null") + ',' + (ls != null ? "{...}" : "null") + ',' + i + ')'));
        }
    }
}
