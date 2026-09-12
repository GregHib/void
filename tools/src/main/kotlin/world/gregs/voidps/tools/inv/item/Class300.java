package world.gregs.voidps.tools.inv.item;/* Class300 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class300 {
    static int anInt3815;
    static boolean aBoolean3819 = false;

    static final Class124 method2277(int i, Class45 class45, int i_5_, int i_6_) {
        if (i_6_ != -1) aBoolean3819 = true;
        anInt3815++;
        byte[] is = class45.method410(-1860, i_5_, i);
        if (is == null) return null;
        return new Class124(is);
    }
}
