package world.gregs.voidps.tools.icon;/* Class300 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class300 {
    static int anInt3815;
    static boolean aBoolean3819 = false;

    static final Mesh load(int i, Js5 js5, int i_5_, int i_6_) {
        if (i_6_ != -1) aBoolean3819 = true;
        anInt3815++;
        byte[] is = js5.getFile(-1860, i_5_, i);
        if (is == null) return null;
        return new Mesh(is);
    }
}
