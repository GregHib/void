package world.gregs.voidps.tools.inv.item;/* Class101_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class101_Sub1 extends Class101 {
    float aFloat5655;
    static int anInt5658;
    static long aLong5663;
    static int anInt5659;
    float aFloat5662;
    float aFloat5664;
    float aFloat5666;
    static int anInt5661;
    float aFloat5669;
    float aFloat5672;
    float aFloat5673;
    static int anInt5667;
    float aFloat5678;
    float aFloat5680;
    float aFloat5681;
    float aFloat5685;
    float aFloat5686;
    static int anInt5688;

    final void method910() {
        anInt5688++;
        this.aFloat5672 = this.aFloat5678 = this.aFloat5664 = 1.0F;
        this.aFloat5655 = this.aFloat5662 = this.aFloat5673 = this.aFloat5680 = this.aFloat5669 = this.aFloat5666 = this.aFloat5686 = this.aFloat5685 = this.aFloat5681 = 0.0F;
    }

    final void method891(int i, int i_17_, int i_18_) {
        this.aFloat5685 += (float) i_17_;
        this.aFloat5681 += (float) i_18_;
        this.aFloat5686 += (float) i;
        anInt5661++;
    }

    final void method900(int i) {
        anInt5658++;
        float f = Class239_Sub4.aFloatArray5876[0x3fff & i];
        float f_24_ = Class239_Sub4.aFloatArray5874[0x3fff & i];
        float f_25_ = this.aFloat5655;
        float f_26_ = this.aFloat5678;
        float f_27_ = this.aFloat5666;
        this.aFloat5655 = f_25_ * f - this.aFloat5662 * f_24_;
        float f_28_ = this.aFloat5685;
        this.aFloat5662 = f_24_ * f_25_ + this.aFloat5662 * f;
        this.aFloat5678 = f * f_26_ - this.aFloat5680 * f_24_;
        this.aFloat5666 = -(f_24_ * this.aFloat5664) + f * f_27_;
        this.aFloat5680 = this.aFloat5680 * f + f_24_ * f_26_;
        this.aFloat5685 = f * f_28_ - f_24_ * this.aFloat5681;
        this.aFloat5664 = f * this.aFloat5664 + f_27_ * f_24_;
        this.aFloat5681 = f_28_ * f_24_ + f * this.aFloat5681;
    }

    final void method902(int i) {
        anInt5667++;
        this.aFloat5664 = 1.0F;
        this.aFloat5672 = this.aFloat5678 = Class239_Sub4.aFloatArray5876[0x3fff & i];
        this.aFloat5655 = Class239_Sub4.aFloatArray5874[0x3fff & i];
        this.aFloat5669 = this.aFloat5686 = this.aFloat5666 = this.aFloat5685 = this.aFloat5662 = this.aFloat5680 = this.aFloat5681 = 0.0F;
        this.aFloat5673 = -this.aFloat5655;
    }

    final void method896(int i) {
        anInt5659++;
        float f = Class239_Sub4.aFloatArray5876[0x3fff & i];
        float f_32_ = Class239_Sub4.aFloatArray5874[i & 0x3fff];
        float f_33_ = this.aFloat5672;
        float f_34_ = this.aFloat5673;
        float f_35_ = this.aFloat5669;
        this.aFloat5672 = f_33_ * f + f_32_ * this.aFloat5662;
        float f_36_ = this.aFloat5686;
        this.aFloat5673 = f * f_34_ + f_32_ * this.aFloat5680;
        this.aFloat5662 = -(f_32_ * f_33_) + this.aFloat5662 * f;
        this.aFloat5669 = f * f_35_ + f_32_ * this.aFloat5664;
        this.aFloat5680 = -(f_32_ * f_34_) + f * this.aFloat5680;
        this.aFloat5664 = -(f_32_ * f_35_) + f * this.aFloat5664;
        this.aFloat5686 = f_36_ * f + f_32_ * this.aFloat5681;
        this.aFloat5681 = f * this.aFloat5681 - f_32_ * f_36_;
    }

    public Class101_Sub1() {
        method910();
    }
}
