package world.gregs.voidps.tools.inv.item;/* ha - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import java.awt.*;
import java.util.Random;

abstract class Toolkit {
    static int anInt4563;
    static int anInt4564;
    static int anInt4565;
    int index;
    static int anInt4573;
    static int anInt4576;
    TextureSource textureSource;
    static int anInt4583;

    abstract void method3652();

    abstract Sprite method3711(int[] is, int i, int i_212_, int i_213_, int i_214_, boolean bool);

    // abstract dependencies of Class213.method1562 (genuine), which calls
    // these on a `ha`-typed reference - all implemented concretely in ha_Sub1.
    abstract Model createModel(Mesh mesh, int functionMask, int featureMask, int ambient, int contrast);

    abstract Matrix method3654();

    abstract void setCamera(Matrix matrix);

    abstract void xa(float f);

    abstract void ZA(int i, float f, float f_573_, float f_574_, float f_575_, float f_576_);

    abstract Matrix method3705();

    abstract int i();

    abstract int XA();

    abstract void f(int i, int i_156_);

    abstract void ya();

    abstract void la();

    abstract void aa(int i, int i_334_, int i_335_, int i_336_, int i_337_, int i_338_);

    abstract int[] na(int i, int i_0_, int i_1_, int i_2_);

    abstract void DA(int i, int i_223_, int i_224_, int i_225_);

    final void method3635(byte i) {
        int i_15_ = -90 % ((i - 8) / 33);
        anInt4573++;
        Class348_Sub40_Sub26.aBooleanArray9351[this.index] = false;
        method3652();
    }

    final Sprite createSprite(int i, int[] is, byte i_84_, int i_85_, int i_86_, int i_87_) {
        anInt4565++;
        if (i_84_ != 94) return null;
        return method3711(is, i_85_, i_86_, i, i_87_, true);
    }

    static final byte[] method3664(int i, int i_88_) {
        anInt4564++;
        if (i_88_ <= 21) anInt4583 = 60;
        Class348_Sub42_Sub3 class348_sub42_sub3 = ((Class348_Sub42_Sub3) Class348_Sub1_Sub2.aClass308_8815.method2302(i, (byte) -120));
        if (class348_sub42_sub3 == null) {
            byte[] is = new byte[512];
            Random random = new Random(i);
            for (int i_89_ = 0; i_89_ < 255; i_89_++)
                is[i_89_] = (byte) i_89_;
            for (int i_90_ = 0; i_90_ < 255; i_90_++) {
                int i_91_ = -i_90_ + 255;
                int i_92_ = Mesh.method1097((byte) 95, i_91_, random);
                byte i_93_ = is[i_92_];
                is[i_92_] = is[i_91_];
                is[i_91_] = is[511 + -i_90_] = i_93_;
            }
            class348_sub42_sub3 = new Class348_Sub42_Sub3(is);
            Class348_Sub1_Sub2.aClass308_8815.method2305(i, class348_sub42_sub3, -1);
        }
        return class348_sub42_sub3.aByteArray9499;
    }

    static final synchronized Toolkit method3692(int i, int i_168_, int i_169_, Js5 js5, int i_170_, TextureSource var_textureSource, Canvas canvas, int i_171_) {
        try {
            anInt4576++;
            // Only the i_170_ == i_171_ branch is ever reachable from this
            // renderer (CacheItemSpriteDumper always calls with both 0); the
            // other renderer-selection branches (Class306/Class262/Class93/
            // Class96) are unreachable per JaCoCo coverage and were dropped.
            if (i_170_ == i_171_) return Class348_Sub5.method2753(true, i_168_, i_169_, canvas, var_textureSource);
            throw new IllegalArgumentException("UM");
        } catch (RuntimeException runtimeexception) {
            throw Class348_Sub17.method2929(runtimeexception, ("ha.TJ(" + i + ',' + i_168_ + ',' + i_169_ + ',' + (js5 != null ? "{...}" : "null") + ',' + i_170_ + ',' + (var_textureSource != null ? "{...}" : "null") + ',' + (canvas != null ? "{...}" : "null") + ',' + i_171_ + ')'));
        }
    }

    Toolkit(TextureSource var_textureSource) {
        this.textureSource = var_textureSource;
        int i = -1;
        for (int i_215_ = 0; i_215_ < 8; i_215_++) {
            if (!Class348_Sub40_Sub26.aBooleanArray9351[i_215_]) {
                Class348_Sub40_Sub26.aBooleanArray9351[i_215_] = true;
                i = i_215_;
                break;
            }
        }
        if (i == -1) throw new IllegalStateException("NFTI");
        this.index = i;
    }
}
