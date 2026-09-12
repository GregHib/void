package world.gregs.voidps.tools.inv.item;/* Class244 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Js5TextureSource implements TextureSource {
    static int anInt4612;
    static int anInt4614;
    static int anInt4616;
    static int anInt4617;
    static int anInt4618;
    static long aLong4615;
    private final TextureMetrics[] textureMetrics;
    static int anInt4620;
    static int anInt4621;
    private final Js5 sprites;
    private final Class308 aClass308_4622 = new Class308(256);
    private final Js5 textures;
    private final int textureCount;

    public final int[] method6(int i, int i_0_, float f, int i_1_, boolean bool, int i_2_) {
        if (i != -21540) getMetrics(-46, 6);
        anInt4617++;
        return method1881(i_1_, false).method3183(this, i_2_, i_0_, (textureMetrics[i_1_].aBoolean207), f, textures, (byte) 11);
    }

    private final Class348_Sub42_Sub5 method1881(int i, boolean bool) {
        anInt4620++;
        Class348_Sub42 class348_sub42 = aClass308_4622.method2302(i, (byte) -34);
        if (class348_sub42 != null) return (Class348_Sub42_Sub5) class348_sub42;
        byte[] is = sprites.getFile((byte) 73, i);
        if (is == null) return null;
        if (bool != false) method1(-58, 1.9039171F, false, -106, -22, -18);
        Class348_Sub42_Sub5 class348_sub42_sub5 = new Class348_Sub42_Sub5(new Packet(is));
        aClass308_4622.method2305(i, class348_sub42_sub5, -1);
        return class348_sub42_sub5;
    }

    public final TextureMetrics getMetrics(int i, int i_3_) {
        anInt4621++;
        if (i_3_ != -6662) return null;
        return textureMetrics[i];
    }

    public final int[] method5(boolean bool, int i, float f, int i_4_, int i_5_, int i_6_) {
        int i_7_ = 41 / ((i_6_ - -69) / 48);
        anInt4614++;
        return method1881(i, false).method3185(i_4_, this, 0, textureMetrics[i].aBoolean207, f, bool, textures, i_5_);
    }

    public final boolean method4(int i, int i_8_) {
        anInt4616++;
        Class348_Sub42_Sub5 class348_sub42_sub5 = method1881(i_8_, false);
        if (i != -7953) method4(56, -109);
        return class348_sub42_sub5 != null && class348_sub42_sub5.method3184(this, textures, -85);
    }

    /* NOTE: method1 is NOT in the genuine-methods list (0 JaCoCo hits) for
     * this renderer, so it is stubbed rather than pulling in
     * Class348_Sub42_Sub5.method3186 (also not genuine) to compile a path
     * that never executes here. */
    public final float[] method1(int i, float f, boolean bool, int i_9_, int i_10_, int i_11_) {
        if (i_9_ != -30824) return null;
        anInt4612++;
        throw new IllegalStateException(); // unreachable per JaCoCo coverage
    }

    public final int method2(boolean bool) {
        anInt4618++;
        if (bool != true) aLong4615 = -52L;
        return textureCount;
    }

    Js5TextureSource(Js5 js5, Js5 js5_12_, Js5 js5_13_) {
        try {
            sprites = js5_12_;
            textures = js5_13_;
            Packet packet = new Packet(js5.getFile(-1860, 0, 0));
            textureCount = packet.readUnsignedShort(842397944);
            textureMetrics = new TextureMetrics[textureCount];
            for (int i = 0; textureCount > i; i++) {
                if (packet.readUnsignedByte(255) == 1) textureMetrics[i] = new TextureMetrics();
            }
            for (int i = 0; i < textureCount; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].disableable = packet.readUnsignedByte(255) == 0;
            }
            for (int i = 0; i < textureCount; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].small = packet.readUnsignedByte(255) == 1;
            }
            for (int i = 0; textureCount > i; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].aBoolean204 = packet.readUnsignedByte(255) == 1;
            }
            for (int i = 0; i < textureCount; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].aByte216 = packet.readByte(-85);
            }
            for (int i = 0; i < textureCount; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].alpha = packet.readByte(-113);
            }
            for (int i = 0; i < textureCount; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].effectType = packet.readByte(-97);
            }
            for (int i = 0; i < textureCount; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].effectParam1 = packet.readByte(-82);
            }
            for (int i = 0; i < textureCount; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].aShort208 = (short) packet.readUnsignedShort(842397944);
            }
            for (int i = 0; textureCount > i; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].speedU = packet.readByte(-86);
            }
            for (int i = 0; textureCount > i; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].speedV = packet.readByte(-104);
            }
            for (int i = 0; textureCount > i; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].aBoolean212 = packet.readUnsignedByte(255) == 1;
            }
            for (int i = 0; textureCount > i; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].aBoolean207 = packet.readUnsignedByte(255) == 1;
            }
            for (int i = 0; i < textureCount; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].aByte205 = packet.readByte(-77);
            }
            for (int i = 0; i < textureCount; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].aBoolean217 = packet.readUnsignedByte(255) == 1;
            }
            for (int i = 0; textureCount > i; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].aBoolean215 = packet.readUnsignedByte(255) == 1;
            }
            for (int i = 0; textureCount > i; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].aBoolean218 = packet.readUnsignedByte(255) == 1;
            }
            for (int i = 0; i < textureCount; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].colourOp = packet.readUnsignedByte(255);
            }
            for (int i = 0; textureCount > i; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].effectParam2 = packet.readInt((byte) -126);
            }
            for (int i = 0; textureCount > i; i++) {
                if (textureMetrics[i] != null) textureMetrics[i].alphaBlendMode = packet.readUnsignedByte(255);
            }
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("tda.<init>(" + (js5 != null ? "{...}" : "null") + ',' + (js5_12_ != null ? "{...}" : "null") + ',' + (js5_13_ != null ? "{...}" : "null") + ')'));
        }
    }
}
