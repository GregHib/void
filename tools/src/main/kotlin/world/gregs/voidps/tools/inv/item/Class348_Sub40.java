package world.gregs.voidps.tools.inv.item;/* Class348_Sub40 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

abstract class Class348_Sub40 {
    static int anInt7027;
    static int anInt7028;
    Class348_Sub40[] aClass348_Sub40Array7031;
    Class191 aClass191_7032;
    Class322 aClass322_7033;
    static int anInt7034;
    static int anInt7035;
    int anInt7036;
    static int anInt7037;
    static int anInt7038;
    static int anInt7039;
    static int anInt7040;
    static int anInt7029;
    static int anInt7043;
    static int anInt7044;
    boolean aBoolean7045;

    // Trimmed for item_renderer_standalone: the original body of method3038
    // pushed flags out to a dozen unrelated subsystems (sound engine, HUD
    // components, minimap, chat, etc.) reached only through the giant
    // Class348_Sub51 client singleton graph -- none of which exist in, or
    // are relevant to, this standalone item-icon renderer. This is also
    // confirmed dead in practice: the only call site
    // (Class348_Sub37.method3031 -> Class348_Sub40.method3049) always
    // passes i_60_ == 31015, so the "if (i_60_ != 31015) method3038(-16);"
    // guard in method3049 below never actually invokes this method.
    static final void method3038(int i) {
        anInt7044++;
    }

    int method3037(int i) {
        if (i >= -113) method3048(-125, -85, 60);
        anInt7027++;
        return -1;
    }

    final int[][] method3039(byte i, int i_1_, int i_2_) {
        anInt7039++;
        int i_3_ = 9 / ((6 - i) / 37);
        if (this.aClass348_Sub40Array7031[i_2_].aBoolean7045) {
            int[] is = this.aClass348_Sub40Array7031[i_2_].method3042(i_1_, 255);
            int[][] is_4_ = new int[3][];
            is_4_[2] = is;
            is_4_[1] = is;
            is_4_[0] = is;
            return is_4_;
        }
        return this.aClass348_Sub40Array7031[i_2_].method3047(i_1_, -1564599039);
    }

    int[] method3042(int i, int i_53_) {
        if (i_53_ != 255) return null;
        anInt7035++;
        throw new IllegalStateException("This operation does not have a monochrome output");
    }

    int method3043(int i) {
        anInt7037++;
        if (i != -1) this.aClass322_7033 = null;
        return -1;
    }

    void method3044(int i) {
        if (i <= 108) this.aClass191_7032 = null;
        anInt7029++;
    }

    void method3045(int i, int i_54_, int i_55_) {
        anInt7043++;
        int i_56_ = (i_55_ != (~this.anInt7036) ? this.anInt7036 : i_54_);
        if (this.aBoolean7045) this.aClass191_7032 = new Class191(i_56_, i_54_, i);
        else this.aClass322_7033 = new Class322(i_56_, i_54_, i);
    }

    void method3046(byte i) {
        anInt7038++;
        if (i > -102) method3046((byte) -112);
        if (this.aBoolean7045) {
            this.aClass191_7032.method1432((byte) 124);
            this.aClass191_7032 = null;
        } else {
            this.aClass322_7033.method2558(6144);
            this.aClass322_7033 = null;
        }
    }

    int[][] method3047(int i, int i_57_) {
        anInt7040++;
        if (i_57_ != -1564599039) method3048(-4, -64, 20);
        throw new IllegalStateException("This operation does not have a colour output");
    }

    final int[] method3048(int i, int i_58_, int i_59_) {
        anInt7034++;
        if (i_58_ != 633706337) this.aClass191_7032 = null;
        if (!this.aClass348_Sub40Array7031[i_59_].aBoolean7045) return (this.aClass348_Sub40Array7031[i_59_].method3047(i, -1564599039)[0]);
        return this.aClass348_Sub40Array7031[i_59_].method3042(i, i_58_ + -633706082);
    }

    void method3049(Packet packet, int i, int i_60_) {
        anInt7028++;
        if (i_60_ != 31015) method3038(-16);
    }


    Class348_Sub40(int i, boolean bool) {
        this.aClass348_Sub40Array7031 = new Class348_Sub40[i];
        this.aBoolean7045 = bool;
    }
}
