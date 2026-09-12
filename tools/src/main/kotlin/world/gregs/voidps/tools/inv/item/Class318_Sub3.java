package world.gregs.voidps.tools.inv.item;/* Class318_Sub3 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class318_Sub3 extends Class318 {
    boolean aBoolean6401 = false;
    int anInt6402;
    int anInt6403;
    int anInt6404;
    int anInt6405;
    int anInt6406;

    final boolean method2500(int i, int i_0_) {
        if (!this.aBoolean6401) return false;
        int i_1_ = (this.anInt6406 - this.anInt6405);
        int i_2_ = (this.anInt6404 - this.anInt6402);
        int i_3_ = i_1_ * i_1_ + i_2_ * i_2_;
        int i_4_ = (i * i_1_ + i_0_ * i_2_ - (this.anInt6405 * i_1_ + this.anInt6402 * i_2_));
        if (i_4_ <= 0) {
            int i_5_ = this.anInt6405 - i;
            int i_6_ = this.anInt6402 - i_0_;
            return i_5_ * i_5_ + i_6_ * i_6_ < (this.anInt6403 * this.anInt6403);
        }
        if (i_4_ > i_3_) {
            int i_7_ = this.anInt6406 - i;
            int i_8_ = this.anInt6404 - i_0_;
            return i_7_ * i_7_ + i_8_ * i_8_ < (this.anInt6403 * this.anInt6403);
        }
        i_4_ = (i_4_ << 10) / i_3_;
        int i_9_ = this.anInt6405 + (i_1_ * i_4_ >> 10) - i;
        int i_10_ = this.anInt6402 + (i_2_ * i_4_ >> 10) - i_0_;
        return i_9_ * i_9_ + i_10_ * i_10_ < (this.anInt6403 * this.anInt6403);
    }

    public Class318_Sub3() {
        /* empty */
    }
}
