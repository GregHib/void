package world.gregs.voidps.tools.inv.item;/* Class109 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Rasterizer {
    private final JavaToolkit aHa_Sub1_1666;
    boolean aBoolean1667 = false;
    int anInt1665;
    int anInt1668;
    boolean aBoolean1669;
    private final JavaThreadResource aJavaThreadResource_1670;
    boolean clamp;
    int height;
    private final int[] anIntArray1673;
    int anInt1674 = 0;
    private boolean aBoolean1675 = false;
    int[] lineOffsets = new int[4096];
    private final float[] aFloatArray1677;
    private final int anInt1678;
    int width;
    private final boolean aBoolean1680;
    private float aFloat1681;
    private float aFloat1682;
    private int anInt1683;
    private float aFloat1684;
    private int[] anIntArray1685;
    private int anInt1686;
    private final int anInt1687;
    private int anInt1688;
    private final int anInt1689;
    private int anInt1690;
    private int anInt1691;
    private int[] anIntArray1692;
    private int anInt1693;
    private boolean aBoolean1694;
    private int anInt1695;
    private int anInt1696;
    private final int anInt1697;
    private int[] anIntArray1698;

    private final void method1016(int[] is, int[] is_0_, int i, int i_1_, int i_2_, float f, float f_3_, float f_4_, float f_5_, float f_6_, float f_7_, float f_8_, float f_9_, float f_10_, float f_11_, float f_12_, float f_13_, float f_14_, float f_15_, float f_16_, float f_17_) {
        int i_18_ = i_2_ - i_1_;
        float f_19_ = 1.0F / (float) i_18_;
        float f_20_ = (f_3_ - f) * f_19_;
        float f_21_ = (f_5_ - f_4_) * f_19_;
        float f_22_ = (f_7_ - f_6_) * f_19_;
        float f_23_ = (f_9_ - f_8_) * f_19_;
        float f_24_ = (f_11_ - f_10_) * f_19_;
        float f_25_ = (f_13_ - f_12_) * f_19_;
        float f_26_ = (f_15_ - f_14_) * f_19_;
        float f_27_ = (f_17_ - f_16_) * f_19_;
        if (this.clamp) {
            if (i_2_ > this.width) i_2_ = this.width;
            if (i_1_ < 0) {
                f -= f_20_ * (float) i_1_;
                f_4_ -= f_21_ * (float) i_1_;
                f_6_ -= f_22_ * (float) i_1_;
                f_8_ -= f_23_ * (float) i_1_;
                f_10_ -= f_24_ * (float) i_1_;
                f_12_ -= f_25_ * (float) i_1_;
                f_14_ -= f_26_ * (float) i_1_;
                f_16_ -= f_27_ * (float) i_1_;
                i_1_ = 0;
            }
        }
        if (i_1_ < i_2_) {
            i_18_ = i_2_ - i_1_;
            i += i_1_;
            while (i_18_-- > 0) {
                float f_28_ = 1.0F / f;
                if (f_28_ < aFloatArray1677[i]) {
                    int i_29_ = (int) (f_4_ * f_28_ * (float) anInt1693);
                    if (aBoolean1694) i_29_ &= anInt1690;
                    else if (i_29_ < 0) i_29_ = 0;
                    else if (i_29_ > anInt1690) i_29_ = anInt1690;
                    int i_30_ = (int) (f_6_ * f_28_ * (float) anInt1693);
                    if (aBoolean1694) i_30_ &= anInt1690;
                    else if (i_30_ < 0) i_30_ = 0;
                    else if (i_30_ > anInt1690) i_30_ = anInt1690;
                    int i_31_ = anIntArray1698[i_30_ * anInt1693 + i_29_];
                    int i_32_ = 255;
                    if (anInt1683 == 2) i_32_ = i_31_ >> 24 & 0xff;
                    else if (anInt1683 == 1) i_32_ = i_31_ == 0 ? 0 : 255;
                    else i_32_ = (int) f_10_;
                    if (i_32_ != 0) {
                        if (i_32_ == 255) {
                            int i_38_ = (~0xffffff | ((int) (f_12_ * (float) (i_31_ >> 16 & 0xff)) << 8 & 0xff0000) | (int) (f_14_ * (float) (i_31_ >> 8 & 0xff)) & 0xff00 | ((int) (f_16_ * (float) (i_31_ & 0xff)) >> 8));
                            if (f_8_ != 0.0F) {
                                int i_39_ = (int) (255.0F - f_8_);
                                int i_40_ = ((((anInt1696 & 0xff00ff) * (int) f_8_ & ~0xff00ff) | ((anInt1696 & 0xff00) * (int) f_8_ & 0xff0000)) >>> 8);
                                i_38_ = (((i_38_ & 0xff00ff) * i_39_ & ~0xff00ff | (i_38_ & 0xff00) * i_39_ & 0xff0000) >>> 8) + i_40_;
                            }
                            is[i] = i_38_;
                            aFloatArray1677[i] = f_28_;
                        } else {
                            int i_33_ = (~0xffffff | ((int) (f_12_ * (float) (i_31_ >> 16 & 0xff)) << 8 & 0xff0000) | (int) (f_14_ * (float) (i_31_ >> 8 & 0xff)) & 0xff00 | ((int) (f_16_ * (float) (i_31_ & 0xff)) >> 8));
                            if (f_8_ != 0.0F) {
                                int i_34_ = (int) (255.0F - f_8_);
                                int i_35_ = ((((anInt1696 & 0xff00ff) * (int) f_8_ & ~0xff00ff) | ((anInt1696 & 0xff00) * (int) f_8_ & 0xff0000)) >>> 8);
                                i_33_ = (((i_33_ & 0xff00ff) * i_34_ & ~0xff00ff | (i_33_ & 0xff00) * i_34_ & 0xff0000) >>> 8) + i_35_;
                            }
                            int i_36_ = is[i];
                            int i_37_ = 255 - i_32_;
                            i_33_ = ((((i_36_ & 0xff00ff) * i_37_ + (i_33_ & 0xff00ff) * i_32_) & ~0xff00ff) + (((i_36_ & 0xff00) * i_37_ + (i_33_ & 0xff00) * i_32_) & 0xff0000)) >> 8;
                            is[i] = i_33_;
                            aFloatArray1677[i] = f_28_;
                        }
                    }
                }
                i++;
                f += f_20_;
                f_4_ += f_21_;
                f_6_ += f_22_;
                f_8_ += f_23_;
                f_10_ += f_24_;
                f_12_ += f_25_;
                f_14_ += f_26_;
                f_16_ += f_27_;
            }
        }
    }

    final int method1017() {
        return this.lineOffsets[0] / anInt1678;
    }

    final void method1018(float f, float f_41_, float f_42_, float f_43_, float f_44_, float f_45_, float f_46_, float f_47_, float f_48_, int i) {
        if (aBoolean1675) {
            aHa_Sub1_1666.line((int) f, (int) f_43_, (int) f_44_, -8003, i, (int) f_41_);
            aHa_Sub1_1666.line((int) f_41_, (int) f_44_, (int) f_45_, -8003, i, (int) f_42_);
            aHa_Sub1_1666.line((int) f_42_, (int) f_45_, (int) f_43_, -8003, i, (int) f);
        } else {
            float f_49_ = f_44_ - f_43_;
            float f_50_ = f_41_ - f;
            float f_51_ = f_45_ - f_43_;
            float f_52_ = f_42_ - f;
            float f_53_ = f_47_ - f_46_;
            float f_54_ = f_48_ - f_46_;
            float f_55_ = 0.0F;
            if (f_41_ != f) f_55_ = (f_44_ - f_43_) / (f_41_ - f);
            float f_56_ = 0.0F;
            if (f_42_ != f_41_) f_56_ = (f_45_ - f_44_) / (f_42_ - f_41_);
            float f_57_ = 0.0F;
            if (f_42_ != f) f_57_ = (f_43_ - f_45_) / (f - f_42_);
            float f_58_ = f_49_ * f_52_ - f_51_ * f_50_;
            if (f_58_ != 0.0F) {
                float f_59_ = (f_53_ * f_52_ - f_54_ * f_50_) / f_58_;
                float f_60_ = (f_54_ * f_49_ - f_53_ * f_51_) / f_58_;
                if (f <= f_41_ && f <= f_42_) {
                    if (!(f >= (float) this.height)) {
                        if (f_41_ > (float) this.height) f_41_ = (float) this.height;
                        if (f_42_ > (float) this.height) f_42_ = (float) this.height;
                        f_46_ = f_46_ - f_59_ * f_43_ + f_59_;
                        if (f_41_ < f_42_) {
                            f_45_ = f_43_;
                            if (f < 0.0F) {
                                f_45_ -= f_57_ * f;
                                f_43_ -= f_55_ * f;
                                f_46_ -= f_60_ * f;
                                f = 0.0F;
                            }
                            if (f_41_ < 0.0F) {
                                f_44_ -= f_56_ * f_41_;
                                f_41_ = 0.0F;
                            }
                            if (f != f_41_ && f_57_ < f_55_ || f == f_41_ && f_57_ > f_56_) {
                                f_42_ -= f_41_;
                                f_41_ -= f;
                                f = (float) (this.lineOffsets[(int) f]);
                                while (--f_41_ >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f, i, 0, (int) f_45_, (int) f_43_, f_46_, f_59_);
                                    f_45_ += f_57_;
                                    f_43_ += f_55_;
                                    f_46_ += f_60_;
                                    f += (float) anInt1678;
                                }
                                while (--f_42_ >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f, i, 0, (int) f_45_, (int) f_44_, f_46_, f_59_);
                                    f_45_ += f_57_;
                                    f_44_ += f_56_;
                                    f_46_ += f_60_;
                                    f += (float) anInt1678;
                                }
                            } else {
                                f_42_ -= f_41_;
                                f_41_ -= f;
                                f = (float) (this.lineOffsets[(int) f]);
                                while (--f_41_ >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f, i, 0, (int) f_43_, (int) f_45_, f_46_, f_59_);
                                    f_45_ += f_57_;
                                    f_43_ += f_55_;
                                    f_46_ += f_60_;
                                    f += (float) anInt1678;
                                }
                                while (--f_42_ >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f, i, 0, (int) f_44_, (int) f_45_, f_46_, f_59_);
                                    f_45_ += f_57_;
                                    f_44_ += f_56_;
                                    f_46_ += f_60_;
                                    f += (float) anInt1678;
                                }
                            }
                        } else {
                            f_44_ = f_43_;
                            if (f < 0.0F) {
                                f_44_ -= f_57_ * f;
                                f_43_ -= f_55_ * f;
                                f_46_ -= f_60_ * f;
                                f = 0.0F;
                            }
                            if (f_42_ < 0.0F) {
                                f_45_ -= f_56_ * f_42_;
                                f_42_ = 0.0F;
                            }
                            if (f != f_42_ && f_57_ < f_55_ || f == f_42_ && f_56_ > f_55_) {
                                f_41_ -= f_42_;
                                f_42_ -= f;
                                f = (float) (this.lineOffsets[(int) f]);
                                while (--f_42_ >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f, i, 0, (int) f_44_, (int) f_43_, f_46_, f_59_);
                                    f_44_ += f_57_;
                                    f_43_ += f_55_;
                                    f_46_ += f_60_;
                                    f += (float) anInt1678;
                                }
                                while (--f_41_ >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f, i, 0, (int) f_45_, (int) f_43_, f_46_, f_59_);
                                    f_45_ += f_56_;
                                    f_43_ += f_55_;
                                    f_46_ += f_60_;
                                    f += (float) anInt1678;
                                }
                            } else {
                                f_41_ -= f_42_;
                                f_42_ -= f;
                                f = (float) (this.lineOffsets[(int) f]);
                                while (--f_42_ >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f, i, 0, (int) f_43_, (int) f_44_, f_46_, f_59_);
                                    f_44_ += f_57_;
                                    f_43_ += f_55_;
                                    f_46_ += f_60_;
                                    f += (float) anInt1678;
                                }
                                while (--f_41_ >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f, i, 0, (int) f_43_, (int) f_45_, f_46_, f_59_);
                                    f_45_ += f_56_;
                                    f_43_ += f_55_;
                                    f_46_ += f_60_;
                                    f += (float) anInt1678;
                                }
                            }
                        }
                    }
                } else if (f_41_ <= f_42_) {
                    if (!(f_41_ >= (float) this.height)) {
                        if (f_42_ > (float) this.height) f_42_ = (float) this.height;
                        if (f > (float) this.height) f = (float) this.height;
                        f_47_ = f_47_ - f_59_ * f_44_ + f_59_;
                        if (f_42_ < f) {
                            f_43_ = f_44_;
                            if (f_41_ < 0.0F) {
                                f_43_ -= f_55_ * f_41_;
                                f_44_ -= f_56_ * f_41_;
                                f_47_ -= f_60_ * f_41_;
                                f_41_ = 0.0F;
                            }
                            if (f_42_ < 0.0F) {
                                f_45_ -= f_57_ * f_42_;
                                f_42_ = 0.0F;
                            }
                            if (f_41_ != f_42_ && f_55_ < f_56_ || f_41_ == f_42_ && f_55_ > f_57_) {
                                f -= f_42_;
                                f_42_ -= f_41_;
                                f_41_ = (float) (this.lineOffsets[(int) f_41_]);
                                while (--f_42_ >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f_41_, i, 0, (int) f_43_, (int) f_44_, f_47_, f_59_);
                                    f_43_ += f_55_;
                                    f_44_ += f_56_;
                                    f_47_ += f_60_;
                                    f_41_ += (float) anInt1678;
                                }
                                while (--f >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f_41_, i, 0, (int) f_43_, (int) f_45_, f_47_, f_59_);
                                    f_43_ += f_55_;
                                    f_45_ += f_57_;
                                    f_47_ += f_60_;
                                    f_41_ += (float) anInt1678;
                                }
                            } else {
                                f -= f_42_;
                                f_42_ -= f_41_;
                                f_41_ = (float) (this.lineOffsets[(int) f_41_]);
                                while (--f_42_ >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f_41_, i, 0, (int) f_44_, (int) f_43_, f_47_, f_59_);
                                    f_43_ += f_55_;
                                    f_44_ += f_56_;
                                    f_47_ += f_60_;
                                    f_41_ += (float) anInt1678;
                                }
                                while (--f >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f_41_, i, 0, (int) f_45_, (int) f_43_, f_47_, f_59_);
                                    f_43_ += f_55_;
                                    f_45_ += f_57_;
                                    f_47_ += f_60_;
                                    f_41_ += (float) anInt1678;
                                }
                            }
                        } else {
                            f_45_ = f_44_;
                            if (f_41_ < 0.0F) {
                                f_45_ -= f_55_ * f_41_;
                                f_44_ -= f_56_ * f_41_;
                                f_47_ -= f_60_ * f_41_;
                                f_41_ = 0.0F;
                            }
                            if (f < 0.0F) {
                                f_43_ -= f_57_ * f;
                                f = 0.0F;
                            }
                            if (f_55_ < f_56_) {
                                f_42_ -= f;
                                f -= f_41_;
                                f_41_ = (float) (this.lineOffsets[(int) f_41_]);
                                while (--f >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f_41_, i, 0, (int) f_45_, (int) f_44_, f_47_, f_59_);
                                    f_45_ += f_55_;
                                    f_44_ += f_56_;
                                    f_47_ += f_60_;
                                    f_41_ += (float) anInt1678;
                                }
                                while (--f_42_ >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f_41_, i, 0, (int) f_43_, (int) f_44_, f_47_, f_59_);
                                    f_43_ += f_57_;
                                    f_44_ += f_56_;
                                    f_47_ += f_60_;
                                    f_41_ += (float) anInt1678;
                                }
                            } else {
                                f_42_ -= f;
                                f -= f_41_;
                                f_41_ = (float) (this.lineOffsets[(int) f_41_]);
                                while (--f >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f_41_, i, 0, (int) f_44_, (int) f_45_, f_47_, f_59_);
                                    f_45_ += f_55_;
                                    f_44_ += f_56_;
                                    f_47_ += f_60_;
                                    f_41_ += (float) anInt1678;
                                }
                                while (--f_42_ >= 0.0F) {
                                    method1026(anIntArray1673, aFloatArray1677, (int) f_41_, i, 0, (int) f_44_, (int) f_43_, f_47_, f_59_);
                                    f_43_ += f_57_;
                                    f_44_ += f_56_;
                                    f_47_ += f_60_;
                                    f_41_ += (float) anInt1678;
                                }
                            }
                        }
                    }
                } else if (!(f_42_ >= (float) this.height)) {
                    if (f > (float) this.height) f = (float) this.height;
                    if (f_41_ > (float) this.height) f_41_ = (float) this.height;
                    f_48_ = f_48_ - f_59_ * f_45_ + f_59_;
                    if (f < f_41_) {
                        f_44_ = f_45_;
                        if (f_42_ < 0.0F) {
                            f_44_ -= f_56_ * f_42_;
                            f_45_ -= f_57_ * f_42_;
                            f_48_ -= f_60_ * f_42_;
                            f_42_ = 0.0F;
                        }
                        if (f < 0.0F) {
                            f_43_ -= f_55_ * f;
                            f = 0.0F;
                        }
                        if (f_56_ < f_57_) {
                            f_41_ -= f;
                            f -= f_42_;
                            f_42_ = (float) (this.lineOffsets[(int) f_42_]);
                            while (--f >= 0.0F) {
                                method1026(anIntArray1673, aFloatArray1677, (int) f_42_, i, 0, (int) f_44_, (int) f_45_, f_48_, f_59_);
                                f_44_ += f_56_;
                                f_45_ += f_57_;
                                f_48_ += f_60_;
                                f_42_ += (float) anInt1678;
                            }
                            while (--f_41_ >= 0.0F) {
                                method1026(anIntArray1673, aFloatArray1677, (int) f_42_, i, 0, (int) f_44_, (int) f_43_, f_48_, f_59_);
                                f_44_ += f_56_;
                                f_43_ += f_55_;
                                f_48_ += f_60_;
                                f_42_ += (float) anInt1678;
                            }
                        } else {
                            f_41_ -= f;
                            f -= f_42_;
                            f_42_ = (float) (this.lineOffsets[(int) f_42_]);
                            while (--f >= 0.0F) {
                                method1026(anIntArray1673, aFloatArray1677, (int) f_42_, i, 0, (int) f_45_, (int) f_44_, f_48_, f_59_);
                                f_44_ += f_56_;
                                f_45_ += f_57_;
                                f_48_ += f_60_;
                                f_42_ += (float) anInt1678;
                            }
                            while (--f_41_ >= 0.0F) {
                                method1026(anIntArray1673, aFloatArray1677, (int) f_42_, i, 0, (int) f_43_, (int) f_44_, f_48_, f_59_);
                                f_44_ += f_56_;
                                f_43_ += f_55_;
                                f_48_ += f_60_;
                                f_42_ += (float) anInt1678;
                            }
                        }
                    } else {
                        f_43_ = f_45_;
                        if (f_42_ < 0.0F) {
                            f_43_ -= f_56_ * f_42_;
                            f_45_ -= f_57_ * f_42_;
                            f_48_ -= f_60_ * f_42_;
                            f_42_ = 0.0F;
                        }
                        if (f_41_ < 0.0F) {
                            f_44_ -= f_55_ * f_41_;
                            f_41_ = 0.0F;
                        }
                        if (f_56_ < f_57_) {
                            f -= f_41_;
                            f_41_ -= f_42_;
                            f_42_ = (float) (this.lineOffsets[(int) f_42_]);
                            while (--f_41_ >= 0.0F) {
                                method1026(anIntArray1673, aFloatArray1677, (int) f_42_, i, 0, (int) f_43_, (int) f_45_, f_48_, f_59_);
                                f_43_ += f_56_;
                                f_45_ += f_57_;
                                f_48_ += f_60_;
                                f_42_ += (float) anInt1678;
                            }
                            while (--f >= 0.0F) {
                                method1026(anIntArray1673, aFloatArray1677, (int) f_42_, i, 0, (int) f_44_, (int) f_45_, f_48_, f_59_);
                                f_44_ += f_55_;
                                f_45_ += f_57_;
                                f_48_ += f_60_;
                                f_42_ += (float) anInt1678;
                            }
                        } else {
                            f -= f_41_;
                            f_41_ -= f_42_;
                            f_42_ = (float) (this.lineOffsets[(int) f_42_]);
                            while (--f_41_ >= 0.0F) {
                                method1026(anIntArray1673, aFloatArray1677, (int) f_42_, i, 0, (int) f_45_, (int) f_43_, f_48_, f_59_);
                                f_43_ += f_56_;
                                f_45_ += f_57_;
                                f_48_ += f_60_;
                                f_42_ += (float) anInt1678;
                            }
                            while (--f >= 0.0F) {
                                method1026(anIntArray1673, aFloatArray1677, (int) f_42_, i, 0, (int) f_45_, (int) f_44_, f_48_, f_59_);
                                f_44_ += f_55_;
                                f_45_ += f_57_;
                                f_48_ += f_60_;
                                f_42_ += (float) anInt1678;
                            }
                        }
                    }
                }
            }
        }
    }

    private final void method1019(int[] is, float[] fs, int i, int i_61_, int i_62_, int i_63_, int i_64_, float f, float f_65_, float f_66_, float f_67_) {
        if (this.clamp) {
            if (i_64_ > this.width) i_64_ = this.width;
            if (i_63_ < 0) i_63_ = 0;
        }
        if (i_63_ < i_64_) {
            i += i_63_ - 1;
            f += f_65_ * (float) i_63_;
            f_66_ += f_67_ * (float) i_63_;
            if (aJavaThreadResource_1670.aBoolean2202) {
                do {
                    if (this.aBoolean1669) {
                        i_62_ = i_64_ - i_63_ >> 2;
                        f_65_ *= 4.0F;
                        if (this.anInt1674 == 0) {
                            if (i_62_ > 0) {
                                do {
                                    i_61_ = ItemSpriteCacheKey.HSV_TO_RGB[(int) f];
                                    f += f_65_;
                                    if (f_66_ < fs[++i]) {
                                        is[i] = i_61_;
                                        fs[i] = f_66_;
                                    }
                                    f_66_ += f_67_;
                                    if (f_66_ < fs[++i]) {
                                        is[i] = i_61_;
                                        fs[i] = f_66_;
                                    }
                                    f_66_ += f_67_;
                                    if (f_66_ < fs[++i]) {
                                        is[i] = i_61_;
                                        fs[i] = f_66_;
                                    }
                                    f_66_ += f_67_;
                                    if (f_66_ < fs[++i]) {
                                        is[i] = i_61_;
                                        fs[i] = f_66_;
                                    }
                                    f_66_ += f_67_;
                                } while (--i_62_ > 0);
                            }
                            i_62_ = i_64_ - i_63_ & 0x3;
                            if (i_62_ > 0) {
                                i_61_ = ItemSpriteCacheKey.HSV_TO_RGB[(int) f];
                                do {
                                    if (f_66_ < fs[++i]) {
                                        is[i] = i_61_;
                                        fs[i] = f_66_;
                                    }
                                    f_66_ += f_67_;
                                } while (--i_62_ > 0);
                            }
                        } else {
                            int i_68_ = this.anInt1674;
                            int i_69_ = 256 - this.anInt1674;
                            if (i_62_ > 0) {
                                do {
                                    i_61_ = ItemSpriteCacheKey.HSV_TO_RGB[(int) f];
                                    f += f_65_;
                                    i_61_ = (((i_61_ & 0xff00ff) * i_69_ >> 8 & 0xff00ff) + ((i_61_ & 0xff00) * i_69_ >> 8 & 0xff00));
                                    if (f_66_ < fs[++i]) {
                                        int i_70_ = is[i];
                                        is[i] = (i_61_ + (((i_70_ & 0xff00ff) * i_68_ >> 8) & 0xff00ff) + ((i_70_ & 0xff00) * i_68_ >> 8 & 0xff00));
                                        fs[i] = f_66_;
                                    }
                                    f_66_ += f_67_;
                                    if (f_66_ < fs[++i]) {
                                        int i_71_ = is[i];
                                        is[i] = (i_61_ + (((i_71_ & 0xff00ff) * i_68_ >> 8) & 0xff00ff) + ((i_71_ & 0xff00) * i_68_ >> 8 & 0xff00));
                                        fs[i] = f_66_;
                                    }
                                    f_66_ += f_67_;
                                    if (f_66_ < fs[++i]) {
                                        int i_72_ = is[i];
                                        is[i] = (i_61_ + (((i_72_ & 0xff00ff) * i_68_ >> 8) & 0xff00ff) + ((i_72_ & 0xff00) * i_68_ >> 8 & 0xff00));
                                        fs[i] = f_66_;
                                    }
                                    f_66_ += f_67_;
                                    if (f_66_ < fs[++i]) {
                                        int i_73_ = is[i];
                                        is[i] = (i_61_ + (((i_73_ & 0xff00ff) * i_68_ >> 8) & 0xff00ff) + ((i_73_ & 0xff00) * i_68_ >> 8 & 0xff00));
                                        fs[i] = f_66_;
                                    }
                                    f_66_ += f_67_;
                                } while (--i_62_ > 0);
                            }
                            i_62_ = i_64_ - i_63_ & 0x3;
                            if (i_62_ <= 0) break;
                            i_61_ = ItemSpriteCacheKey.HSV_TO_RGB[(int) f];
                            i_61_ = (((i_61_ & 0xff00ff) * i_69_ >> 8 & 0xff00ff) + ((i_61_ & 0xff00) * i_69_ >> 8 & 0xff00));
                            do {
                                if (f_66_ < fs[++i]) {
                                    int i_74_ = is[i];
                                    is[i] = (i_61_ + ((i_74_ & 0xff00ff) * i_68_ >> 8 & 0xff00ff) + ((i_74_ & 0xff00) * i_68_ >> 8 & 0xff00));
                                    fs[i] = f_66_;
                                }
                                f_66_ += f_67_;
                            } while (--i_62_ > 0);
                        }
                        break;
                    }
                    i_62_ = i_64_ - i_63_;
                    if (this.anInt1674 == 0) {
                        do {
                            if (f_66_ < fs[++i]) {
                                is[i] = ItemSpriteCacheKey.HSV_TO_RGB[(int) f];
                                fs[i] = f_66_;
                            }
                            f_66_ += f_67_;
                            f += f_65_;
                        } while (--i_62_ > 0);
                        break;
                    }
                    int i_75_ = this.anInt1674;
                    int i_76_ = 256 - this.anInt1674;
                    do {
                        if (f_66_ < fs[++i]) {
                            i_61_ = ItemSpriteCacheKey.HSV_TO_RGB[(int) f];
                            i_61_ = (((i_61_ & 0xff00ff) * i_76_ >> 8 & 0xff00ff) + ((i_61_ & 0xff00) * i_76_ >> 8 & 0xff00));
                            int i_77_ = is[i];
                            is[i] = (i_61_ + ((i_77_ & 0xff00ff) * i_75_ >> 8 & 0xff00ff) + ((i_77_ & 0xff00) * i_75_ >> 8 & 0xff00));
                            fs[i] = f_66_;
                        }
                        f += f_65_;
                        f_66_ += f_67_;
                    } while (--i_62_ > 0);
                } while (false);
            } else {
                do {
                    if (this.aBoolean1669) {
                        i_62_ = i_64_ - i_63_ >> 2;
                        f_65_ *= 4.0F;
                        if (this.anInt1674 == 0) {
                            if (i_62_ > 0) {
                                do {
                                    i_61_ = ItemSpriteCacheKey.HSV_TO_RGB[(int) f];
                                    f += f_65_;
                                    if (f_66_ < fs[++i]) is[i] = i_61_;
                                    f_66_ += f_67_;
                                    if (f_66_ < fs[++i]) is[i] = i_61_;
                                    f_66_ += f_67_;
                                    if (f_66_ < fs[++i]) is[i] = i_61_;
                                    f_66_ += f_67_;
                                    if (f_66_ < fs[++i]) is[i] = i_61_;
                                    f_66_ += f_67_;
                                } while (--i_62_ > 0);
                            }
                            i_62_ = i_64_ - i_63_ & 0x3;
                            if (i_62_ > 0) {
                                i_61_ = ItemSpriteCacheKey.HSV_TO_RGB[(int) f];
                                do {
                                    if (f_66_ < fs[++i]) is[i] = i_61_;
                                    f_66_ += f_67_;
                                } while (--i_62_ > 0);
                            }
                        } else {
                            int i_78_ = this.anInt1674;
                            int i_79_ = 256 - this.anInt1674;
                            if (i_62_ > 0) {
                                do {
                                    i_61_ = ItemSpriteCacheKey.HSV_TO_RGB[(int) f];
                                    f += f_65_;
                                    i_61_ = (((i_61_ & 0xff00ff) * i_79_ >> 8 & 0xff00ff) + ((i_61_ & 0xff00) * i_79_ >> 8 & 0xff00));
                                    if (f_66_ < fs[++i]) {
                                        int i_80_ = is[i];
                                        is[i] = (i_61_ + (((i_80_ & 0xff00ff) * i_78_ >> 8) & 0xff00ff) + ((i_80_ & 0xff00) * i_78_ >> 8 & 0xff00));
                                    }
                                    f_66_ += f_67_;
                                    if (f_66_ < fs[++i]) {
                                        int i_81_ = is[i];
                                        is[i] = (i_61_ + (((i_81_ & 0xff00ff) * i_78_ >> 8) & 0xff00ff) + ((i_81_ & 0xff00) * i_78_ >> 8 & 0xff00));
                                    }
                                    f_66_ += f_67_;
                                    if (f_66_ < fs[++i]) {
                                        int i_82_ = is[i];
                                        is[i] = (i_61_ + (((i_82_ & 0xff00ff) * i_78_ >> 8) & 0xff00ff) + ((i_82_ & 0xff00) * i_78_ >> 8 & 0xff00));
                                    }
                                    f_66_ += f_67_;
                                    if (f_66_ < fs[++i]) {
                                        int i_83_ = is[i];
                                        is[i] = (i_61_ + (((i_83_ & 0xff00ff) * i_78_ >> 8) & 0xff00ff) + ((i_83_ & 0xff00) * i_78_ >> 8 & 0xff00));
                                    }
                                    f_66_ += f_67_;
                                } while (--i_62_ > 0);
                            }
                            i_62_ = i_64_ - i_63_ & 0x3;
                            if (i_62_ <= 0) break;
                            i_61_ = ItemSpriteCacheKey.HSV_TO_RGB[(int) f];
                            i_61_ = (((i_61_ & 0xff00ff) * i_79_ >> 8 & 0xff00ff) + ((i_61_ & 0xff00) * i_79_ >> 8 & 0xff00));
                            do {
                                if (f_66_ < fs[++i]) {
                                    int i_84_ = is[i];
                                    is[i] = (i_61_ + ((i_84_ & 0xff00ff) * i_78_ >> 8 & 0xff00ff) + ((i_84_ & 0xff00) * i_78_ >> 8 & 0xff00));
                                }
                                f_66_ += f_67_;
                            } while (--i_62_ > 0);
                        }
                        break;
                    }
                    i_62_ = i_64_ - i_63_;
                    if (this.anInt1674 == 0) {
                        do {
                            if (f_66_ < fs[++i]) is[i] = ItemSpriteCacheKey.HSV_TO_RGB[(int) f];
                            f_66_ += f_67_;
                            f += f_65_;
                        } while (--i_62_ > 0);
                        break;
                    }
                    int i_85_ = this.anInt1674;
                    int i_86_ = 256 - this.anInt1674;
                    do {
                        if (f_66_ < fs[++i]) {
                            i_61_ = ItemSpriteCacheKey.HSV_TO_RGB[(int) f];
                            i_61_ = (((i_61_ & 0xff00ff) * i_86_ >> 8 & 0xff00ff) + ((i_61_ & 0xff00) * i_86_ >> 8 & 0xff00));
                            int i_87_ = is[i];
                            is[i] = (i_61_ + ((i_87_ & 0xff00ff) * i_85_ >> 8 & 0xff00ff) + ((i_87_ & 0xff00) * i_85_ >> 8 & 0xff00));
                        }
                        f += f_65_;
                        f_66_ += f_67_;
                    } while (--i_62_ > 0);
                } while (false);
            }
        }
    }

    private final void method1021(int[] is, float[] fs, int i, int i_168_, int i_169_, int i_170_, int i_171_, float f, float f_172_, float f_173_, float f_174_, float f_175_, float f_176_, float f_177_, float f_178_) {
        if (this.clamp) {
            if (i_171_ > this.width) i_171_ = this.width;
            if (i_170_ < 0) i_170_ = 0;
        }
        if (i_170_ < i_171_) {
            if (aBoolean1680) {
                i += i_170_;
                f_173_ += f_174_ * (float) i_170_;
                f_175_ += f_176_ * (float) i_170_;
                f_177_ += f_178_ * (float) i_170_;
                if (this.aBoolean1669) {
                    i_169_ = i_171_ - i_170_ >> 2;
                    f_174_ *= 4.0F;
                    f_176_ *= 4.0F;
                    f_178_ *= 4.0F;
                    if (this.anInt1674 == 0) {
                        if (i_169_ > 0) {
                            do {
                                i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                f_173_ += f_174_;
                                f_175_ += f_176_;
                                f_177_ += f_178_;
                                is[i++] = i_168_;
                                is[i++] = i_168_;
                                is[i++] = i_168_;
                                is[i++] = i_168_;
                            } while (--i_169_ > 0);
                        }
                        i_169_ = i_171_ - i_170_ & 0x3;
                        if (i_169_ > 0) {
                            i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                            do is[i++] = i_168_; while (--i_169_ > 0);
                        }
                    } else if (this.aBoolean1667) {
                        if (i_169_ > 0) {
                            do {
                                i_168_ = ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                f_173_ += f_174_;
                                f_175_ += f_176_;
                                f_177_ += f_178_;
                                int[] is_183_ = is;
                                int i_184_ = i++;
                                int i_185_ = i_168_;
                                int i_186_ = is_183_[i_184_];
                                int i_187_ = i_185_ + i_186_;
                                int i_188_ = ((i_185_ & 0xff00ff) + (i_186_ & 0xff00ff));
                                i_186_ = (i_188_ & 0x1000100) + (i_187_ - i_188_ & 0x10000);
                                is_183_[i_184_] = (~0xffffff | i_187_ - i_186_ | i_186_ - (i_186_ >>> 8));
                                int[] is_189_ = is;
                                int i_190_ = i++;
                                int i_191_ = i_168_;
                                int i_192_ = is_189_[i_190_];
                                int i_193_ = i_191_ + i_192_;
                                int i_194_ = ((i_191_ & 0xff00ff) + (i_192_ & 0xff00ff));
                                i_192_ = (i_194_ & 0x1000100) + (i_193_ - i_194_ & 0x10000);
                                is_189_[i_190_] = (~0xffffff | i_193_ - i_192_ | i_192_ - (i_192_ >>> 8));
                                int[] is_195_ = is;
                                int i_196_ = i++;
                                int i_197_ = i_168_;
                                int i_198_ = is_195_[i_196_];
                                int i_199_ = i_197_ + i_198_;
                                int i_200_ = ((i_197_ & 0xff00ff) + (i_198_ & 0xff00ff));
                                i_198_ = (i_200_ & 0x1000100) + (i_199_ - i_200_ & 0x10000);
                                is_195_[i_196_] = (~0xffffff | i_199_ - i_198_ | i_198_ - (i_198_ >>> 8));
                                int[] is_201_ = is;
                                int i_202_ = i++;
                                int i_203_ = i_168_;
                                int i_204_ = is_201_[i_202_];
                                int i_205_ = i_203_ + i_204_;
                                int i_206_ = ((i_203_ & 0xff00ff) + (i_204_ & 0xff00ff));
                                i_204_ = (i_206_ & 0x1000100) + (i_205_ - i_206_ & 0x10000);
                                is_201_[i_202_] = (~0xffffff | i_205_ - i_204_ | i_204_ - (i_204_ >>> 8));
                            } while (--i_169_ > 0);
                        }
                        i_169_ = i_171_ - i_170_ & 0x3;
                        if (i_169_ > 0) {
                            i_168_ = ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                            do {
                                int[] is_207_ = is;
                                int i_208_ = i++;
                                int i_209_ = i_168_;
                                int i_210_ = is_207_[i_208_];
                                int i_211_ = i_209_ + i_210_;
                                int i_212_ = ((i_209_ & 0xff00ff) + (i_210_ & 0xff00ff));
                                i_210_ = (i_212_ & 0x1000100) + (i_211_ - i_212_ & 0x10000);
                                is_207_[i_208_] = (~0xffffff | i_211_ - i_210_ | i_210_ - (i_210_ >>> 8));
                            } while (--i_169_ > 0);
                        }
                    } else {
                        int i_179_ = this.anInt1674;
                        int i_180_ = 256 - this.anInt1674;
                        if (i_169_ > 0) {
                            do {
                                i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                f_173_ += f_174_;
                                f_175_ += f_176_;
                                f_177_ += f_178_;
                                i_168_ = (((i_168_ & 0xff00ff) * i_180_ >> 8 & 0xff00ff) + ((i_168_ & 0xff00) * i_180_ >> 8 & 0xff00));
                                int i_181_ = is[i];
                                is[i++] = (i_168_ + ((i_181_ & 0xff00ff) * i_179_ >> 8 & 0xff00ff) + ((i_181_ & 0xff00) * i_179_ >> 8 & 0xff00));
                                i_181_ = is[i];
                                is[i++] = (i_168_ + ((i_181_ & 0xff00ff) * i_179_ >> 8 & 0xff00ff) + ((i_181_ & 0xff00) * i_179_ >> 8 & 0xff00));
                                i_181_ = is[i];
                                is[i++] = (i_168_ + ((i_181_ & 0xff00ff) * i_179_ >> 8 & 0xff00ff) + ((i_181_ & 0xff00) * i_179_ >> 8 & 0xff00));
                                i_181_ = is[i];
                                is[i++] = (i_168_ + ((i_181_ & 0xff00ff) * i_179_ >> 8 & 0xff00ff) + ((i_181_ & 0xff00) * i_179_ >> 8 & 0xff00));
                            } while (--i_169_ > 0);
                        }
                        i_169_ = i_171_ - i_170_ & 0x3;
                        if (i_169_ > 0) {
                            i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                            i_168_ = (((i_168_ & 0xff00ff) * i_180_ >> 8 & 0xff00ff) + ((i_168_ & 0xff00) * i_180_ >> 8 & 0xff00));
                            do {
                                int i_182_ = is[i];
                                is[i++] = (i_168_ + ((i_182_ & 0xff00ff) * i_179_ >> 8 & 0xff00ff) + ((i_182_ & 0xff00) * i_179_ >> 8 & 0xff00));
                            } while (--i_169_ > 0);
                        }
                    }
                } else {
                    i_169_ = i_171_ - i_170_;
                    if (this.anInt1674 == 0) {
                        do {
                            is[i++] = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                            f_173_ += f_174_;
                            f_175_ += f_176_;
                            f_177_ += f_178_;
                        } while (--i_169_ > 0);
                    } else if (this.aBoolean1667) {
                        do {
                            int[] is_216_ = is;
                            int i_217_ = i++;
                            int i_218_ = ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                            int i_219_ = is_216_[i_217_];
                            int i_220_ = i_218_ + i_219_;
                            int i_221_ = (i_218_ & 0xff00ff) + (i_219_ & 0xff00ff);
                            i_219_ = (i_221_ & 0x1000100) + (i_220_ - i_221_ & 0x10000);
                            is_216_[i_217_] = (~0xffffff | i_220_ - i_219_ | i_219_ - (i_219_ >>> 8));
                            f_173_ += f_174_;
                            f_175_ += f_176_;
                            f_177_ += f_178_;
                        } while (--i_169_ > 0);
                    } else {
                        int i_213_ = this.anInt1674;
                        int i_214_ = 256 - this.anInt1674;
                        do {
                            i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                            f_173_ += f_174_;
                            f_175_ += f_176_;
                            f_177_ += f_178_;
                            i_168_ = (((i_168_ & 0xff00ff) * i_214_ >> 8 & 0xff00ff) + ((i_168_ & 0xff00) * i_214_ >> 8 & 0xff00));
                            int i_215_ = is[i];
                            is[i++] = (i_168_ + ((i_215_ & 0xff00ff) * i_213_ >> 8 & 0xff00ff) + ((i_215_ & 0xff00) * i_213_ >> 8 & 0xff00));
                        } while (--i_169_ > 0);
                    }
                }
            } else {
                i += i_170_ - 1;
                f += f_172_ * (float) i_170_;
                f_173_ += f_174_ * (float) i_170_;
                f_175_ += f_176_ * (float) i_170_;
                f_177_ += f_178_ * (float) i_170_;
                if (aJavaThreadResource_1670.aBoolean2202) {
                    if (this.aBoolean1669) {
                        i_169_ = i_171_ - i_170_ >> 2;
                        f_174_ *= 4.0F;
                        f_176_ *= 4.0F;
                        f_178_ *= 4.0F;
                        if (this.anInt1674 == 0) {
                            if (i_169_ > 0) {
                                do {
                                    i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                    f_173_ += f_174_;
                                    f_175_ += f_176_;
                                    f_177_ += f_178_;
                                    if (f < fs[++i]) {
                                        is[i] = i_168_;
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                    if (f < fs[++i]) {
                                        is[i] = i_168_;
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                    if (f < fs[++i]) {
                                        is[i] = i_168_;
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                    if (f < fs[++i]) {
                                        is[i] = i_168_;
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                } while (--i_169_ > 0);
                            }
                            i_169_ = i_171_ - i_170_ & 0x3;
                            if (i_169_ > 0) {
                                i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                do {
                                    if (f < fs[++i]) {
                                        is[i] = i_168_;
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                } while (--i_169_ > 0);
                            }
                        } else if (this.aBoolean1667) {
                            if (i_169_ > 0) {
                                do {
                                    i_168_ = ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                    f_173_ += f_174_;
                                    f_175_ += f_176_;
                                    f_177_ += f_178_;
                                    if (f < fs[++i]) {
                                        int[] is_229_ = is;
                                        int i_230_ = i;
                                        int i_231_ = i_168_;
                                        int i_232_ = is_229_[i_230_];
                                        int i_233_ = i_231_ + i_232_;
                                        int i_234_ = ((i_231_ & 0xff00ff) + (i_232_ & 0xff00ff));
                                        i_232_ = ((i_234_ & 0x1000100) + (i_233_ - i_234_ & 0x10000));
                                        is_229_[i_230_] = (~0xffffff | i_233_ - i_232_ | i_232_ - (i_232_ >>> 8));
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                    if (f < fs[++i]) {
                                        int[] is_235_ = is;
                                        int i_236_ = i;
                                        int i_237_ = i_168_;
                                        int i_238_ = is_235_[i_236_];
                                        int i_239_ = i_237_ + i_238_;
                                        int i_240_ = ((i_237_ & 0xff00ff) + (i_238_ & 0xff00ff));
                                        i_238_ = ((i_240_ & 0x1000100) + (i_239_ - i_240_ & 0x10000));
                                        is_235_[i_236_] = (~0xffffff | i_239_ - i_238_ | i_238_ - (i_238_ >>> 8));
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                    if (f < fs[++i]) {
                                        int[] is_241_ = is;
                                        int i_242_ = i;
                                        int i_243_ = i_168_;
                                        int i_244_ = is_241_[i_242_];
                                        int i_245_ = i_243_ + i_244_;
                                        int i_246_ = ((i_243_ & 0xff00ff) + (i_244_ & 0xff00ff));
                                        i_244_ = ((i_246_ & 0x1000100) + (i_245_ - i_246_ & 0x10000));
                                        is_241_[i_242_] = (~0xffffff | i_245_ - i_244_ | i_244_ - (i_244_ >>> 8));
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                    if (f < fs[++i]) {
                                        int[] is_247_ = is;
                                        int i_248_ = i;
                                        int i_249_ = i_168_;
                                        int i_250_ = is_247_[i_248_];
                                        int i_251_ = i_249_ + i_250_;
                                        int i_252_ = ((i_249_ & 0xff00ff) + (i_250_ & 0xff00ff));
                                        i_250_ = ((i_252_ & 0x1000100) + (i_251_ - i_252_ & 0x10000));
                                        is_247_[i_248_] = (~0xffffff | i_251_ - i_250_ | i_250_ - (i_250_ >>> 8));
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                } while (--i_169_ > 0);
                            }
                            i_169_ = i_171_ - i_170_ & 0x3;
                            if (i_169_ > 0) {
                                i_168_ = ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                do {
                                    if (f < fs[++i]) {
                                        int[] is_253_ = is;
                                        int i_254_ = i;
                                        int i_255_ = i_168_;
                                        int i_256_ = is_253_[i_254_];
                                        int i_257_ = i_255_ + i_256_;
                                        int i_258_ = ((i_255_ & 0xff00ff) + (i_256_ & 0xff00ff));
                                        i_256_ = ((i_258_ & 0x1000100) + (i_257_ - i_258_ & 0x10000));
                                        is_253_[i_254_] = (~0xffffff | i_257_ - i_256_ | i_256_ - (i_256_ >>> 8));
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                } while (--i_169_ > 0);
                            }
                        } else {
                            int i_222_ = this.anInt1674;
                            int i_223_ = 256 - this.anInt1674;
                            if (i_169_ > 0) {
                                do {
                                    i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                    f_173_ += f_174_;
                                    f_175_ += f_176_;
                                    f_177_ += f_178_;
                                    i_168_ = (((i_168_ & 0xff00ff) * i_223_ >> 8 & 0xff00ff) + ((i_168_ & 0xff00) * i_223_ >> 8 & 0xff00));
                                    if (f < fs[++i]) {
                                        int i_224_ = is[i];
                                        is[i] = (i_168_ + (((i_224_ & 0xff00ff) * i_222_ >> 8) & 0xff00ff) + (((i_224_ & 0xff00) * i_222_ >> 8) & 0xff00));
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                    if (f < fs[++i]) {
                                        int i_225_ = is[i];
                                        is[i] = (i_168_ + (((i_225_ & 0xff00ff) * i_222_ >> 8) & 0xff00ff) + (((i_225_ & 0xff00) * i_222_ >> 8) & 0xff00));
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                    if (f < fs[++i]) {
                                        int i_226_ = is[i];
                                        is[i] = (i_168_ + (((i_226_ & 0xff00ff) * i_222_ >> 8) & 0xff00ff) + (((i_226_ & 0xff00) * i_222_ >> 8) & 0xff00));
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                    if (f < fs[++i]) {
                                        int i_227_ = is[i];
                                        is[i] = (i_168_ + (((i_227_ & 0xff00ff) * i_222_ >> 8) & 0xff00ff) + (((i_227_ & 0xff00) * i_222_ >> 8) & 0xff00));
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                } while (--i_169_ > 0);
                            }
                            i_169_ = i_171_ - i_170_ & 0x3;
                            if (i_169_ > 0) {
                                i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                i_168_ = (((i_168_ & 0xff00ff) * i_223_ >> 8 & 0xff00ff) + ((i_168_ & 0xff00) * i_223_ >> 8 & 0xff00));
                                do {
                                    if (f < fs[++i]) {
                                        int i_228_ = is[i];
                                        is[i] = (i_168_ + (((i_228_ & 0xff00ff) * i_222_ >> 8) & 0xff00ff) + (((i_228_ & 0xff00) * i_222_ >> 8) & 0xff00));
                                        fs[i] = f;
                                    }
                                    f += f_172_;
                                } while (--i_169_ > 0);
                            }
                        }
                    } else {
                        i_169_ = i_171_ - i_170_;
                        if (this.anInt1674 == 0) {
                            do {
                                if (f < fs[++i]) {
                                    is[i] = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                    fs[i] = f;
                                }
                                f += f_172_;
                                f_173_ += f_174_;
                                f_175_ += f_176_;
                                f_177_ += f_178_;
                            } while (--i_169_ > 0);
                        } else if (this.aBoolean1667) {
                            do {
                                if (f < fs[++i]) {
                                    int[] is_262_ = is;
                                    int i_263_ = i;
                                    int i_264_ = ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                    int i_265_ = is_262_[i_263_];
                                    int i_266_ = i_264_ + i_265_;
                                    int i_267_ = ((i_264_ & 0xff00ff) + (i_265_ & 0xff00ff));
                                    i_265_ = ((i_267_ & 0x1000100) + (i_266_ - i_267_ & 0x10000));
                                    is_262_[i_263_] = (~0xffffff | i_266_ - i_265_ | i_265_ - (i_265_ >>> 8));
                                    fs[i] = f;
                                }
                                f += f_172_;
                                f_173_ += f_174_;
                                f_175_ += f_176_;
                                f_177_ += f_178_;
                            } while (--i_169_ > 0);
                        } else {
                            int i_259_ = this.anInt1674;
                            int i_260_ = 256 - this.anInt1674;
                            do {
                                if (f < fs[++i]) {
                                    i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                    i_168_ = (((i_168_ & 0xff00ff) * i_260_ >> 8 & 0xff00ff) + ((i_168_ & 0xff00) * i_260_ >> 8 & 0xff00));
                                    int i_261_ = is[i];
                                    is[i] = (i_168_ + ((i_261_ & 0xff00ff) * i_259_ >> 8 & 0xff00ff) + ((i_261_ & 0xff00) * i_259_ >> 8 & 0xff00));
                                    fs[i] = f;
                                }
                                f += f_172_;
                                f_173_ += f_174_;
                                f_175_ += f_176_;
                                f_177_ += f_178_;
                            } while (--i_169_ > 0);
                        }
                    }
                } else if (this.aBoolean1669) {
                    i_169_ = i_171_ - i_170_ >> 2;
                    f_174_ *= 4.0F;
                    f_176_ *= 4.0F;
                    f_178_ *= 4.0F;
                    if (this.anInt1674 == 0) {
                        if (i_169_ > 0) {
                            do {
                                i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                f_173_ += f_174_;
                                f_175_ += f_176_;
                                f_177_ += f_178_;
                                if (f < fs[++i]) is[i] = i_168_;
                                f += f_172_;
                                if (f < fs[++i]) is[i] = i_168_;
                                f += f_172_;
                                if (f < fs[++i]) is[i] = i_168_;
                                f += f_172_;
                                if (f < fs[++i]) is[i] = i_168_;
                                f += f_172_;
                            } while (--i_169_ > 0);
                        }
                        i_169_ = i_171_ - i_170_ & 0x3;
                        if (i_169_ > 0) {
                            i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                            do {
                                if (f < fs[++i]) is[i] = i_168_;
                                f += f_172_;
                            } while (--i_169_ > 0);
                        }
                    } else if (this.aBoolean1667) {
                        if (i_169_ > 0) {
                            do {
                                i_168_ = ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                f_173_ += f_174_;
                                f_175_ += f_176_;
                                f_177_ += f_178_;
                                if (f < fs[++i]) {
                                    int[] is_275_ = is;
                                    int i_276_ = i;
                                    int i_277_ = i_168_;
                                    int i_278_ = is_275_[i_276_];
                                    int i_279_ = i_277_ + i_278_;
                                    int i_280_ = ((i_277_ & 0xff00ff) + (i_278_ & 0xff00ff));
                                    i_278_ = ((i_280_ & 0x1000100) + (i_279_ - i_280_ & 0x10000));
                                    is_275_[i_276_] = (~0xffffff | i_279_ - i_278_ | i_278_ - (i_278_ >>> 8));
                                }
                                f += f_172_;
                                if (f < fs[++i]) {
                                    int[] is_281_ = is;
                                    int i_282_ = i;
                                    int i_283_ = i_168_;
                                    int i_284_ = is_281_[i_282_];
                                    int i_285_ = i_283_ + i_284_;
                                    int i_286_ = ((i_283_ & 0xff00ff) + (i_284_ & 0xff00ff));
                                    i_284_ = ((i_286_ & 0x1000100) + (i_285_ - i_286_ & 0x10000));
                                    is_281_[i_282_] = (~0xffffff | i_285_ - i_284_ | i_284_ - (i_284_ >>> 8));
                                }
                                f += f_172_;
                                if (f < fs[++i]) {
                                    int[] is_287_ = is;
                                    int i_288_ = i;
                                    int i_289_ = i_168_;
                                    int i_290_ = is_287_[i_288_];
                                    int i_291_ = i_289_ + i_290_;
                                    int i_292_ = ((i_289_ & 0xff00ff) + (i_290_ & 0xff00ff));
                                    i_290_ = ((i_292_ & 0x1000100) + (i_291_ - i_292_ & 0x10000));
                                    is_287_[i_288_] = (~0xffffff | i_291_ - i_290_ | i_290_ - (i_290_ >>> 8));
                                }
                                f += f_172_;
                                if (f < fs[++i]) {
                                    int[] is_293_ = is;
                                    int i_294_ = i;
                                    int i_295_ = i_168_;
                                    int i_296_ = is_293_[i_294_];
                                    int i_297_ = i_295_ + i_296_;
                                    int i_298_ = ((i_295_ & 0xff00ff) + (i_296_ & 0xff00ff));
                                    i_296_ = ((i_298_ & 0x1000100) + (i_297_ - i_298_ & 0x10000));
                                    is_293_[i_294_] = (~0xffffff | i_297_ - i_296_ | i_296_ - (i_296_ >>> 8));
                                }
                                f += f_172_;
                            } while (--i_169_ > 0);
                        }
                        i_169_ = i_171_ - i_170_ & 0x3;
                        if (i_169_ > 0) {
                            i_168_ = ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                            do {
                                if (f < fs[++i]) {
                                    int[] is_299_ = is;
                                    int i_300_ = i;
                                    int i_301_ = i_168_;
                                    int i_302_ = is_299_[i_300_];
                                    int i_303_ = i_301_ + i_302_;
                                    int i_304_ = ((i_301_ & 0xff00ff) + (i_302_ & 0xff00ff));
                                    i_302_ = ((i_304_ & 0x1000100) + (i_303_ - i_304_ & 0x10000));
                                    is_299_[i_300_] = (~0xffffff | i_303_ - i_302_ | i_302_ - (i_302_ >>> 8));
                                }
                                f += f_172_;
                            } while (--i_169_ > 0);
                        }
                    } else {
                        int i_268_ = this.anInt1674;
                        int i_269_ = 256 - this.anInt1674;
                        if (i_169_ > 0) {
                            do {
                                i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                f_173_ += f_174_;
                                f_175_ += f_176_;
                                f_177_ += f_178_;
                                i_168_ = (((i_168_ & 0xff00ff) * i_269_ >> 8 & 0xff00ff) + ((i_168_ & 0xff00) * i_269_ >> 8 & 0xff00));
                                if (f < fs[++i]) {
                                    int i_270_ = is[i];
                                    is[i] = (i_168_ + ((i_270_ & 0xff00ff) * i_268_ >> 8 & 0xff00ff) + ((i_270_ & 0xff00) * i_268_ >> 8 & 0xff00));
                                }
                                f += f_172_;
                                if (f < fs[++i]) {
                                    int i_271_ = is[i];
                                    is[i] = (i_168_ + ((i_271_ & 0xff00ff) * i_268_ >> 8 & 0xff00ff) + ((i_271_ & 0xff00) * i_268_ >> 8 & 0xff00));
                                }
                                f += f_172_;
                                if (f < fs[++i]) {
                                    int i_272_ = is[i];
                                    is[i] = (i_168_ + ((i_272_ & 0xff00ff) * i_268_ >> 8 & 0xff00ff) + ((i_272_ & 0xff00) * i_268_ >> 8 & 0xff00));
                                }
                                f += f_172_;
                                if (f < fs[++i]) {
                                    int i_273_ = is[i];
                                    is[i] = (i_168_ + ((i_273_ & 0xff00ff) * i_268_ >> 8 & 0xff00ff) + ((i_273_ & 0xff00) * i_268_ >> 8 & 0xff00));
                                }
                                f += f_172_;
                            } while (--i_169_ > 0);
                        }
                        i_169_ = i_171_ - i_170_ & 0x3;
                        if (i_169_ > 0) {
                            i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                            i_168_ = (((i_168_ & 0xff00ff) * i_269_ >> 8 & 0xff00ff) + ((i_168_ & 0xff00) * i_269_ >> 8 & 0xff00));
                            do {
                                if (f < fs[++i]) {
                                    int i_274_ = is[i];
                                    is[i] = (i_168_ + ((i_274_ & 0xff00ff) * i_268_ >> 8 & 0xff00ff) + ((i_274_ & 0xff00) * i_268_ >> 8 & 0xff00));
                                }
                                f += f_172_;
                            } while (--i_169_ > 0);
                        }
                    }
                } else {
                    i_169_ = i_171_ - i_170_;
                    if (this.anInt1674 == 0) {
                        do {
                            if (f < fs[++i]) is[i] = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                            f += f_172_;
                            f_173_ += f_174_;
                            f_175_ += f_176_;
                            f_177_ += f_178_;
                        } while (--i_169_ > 0);
                    } else if (this.aBoolean1667) {
                        do {
                            if (f < fs[++i]) {
                                int[] is_308_ = is;
                                int i_309_ = i;
                                int i_310_ = ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                int i_311_ = is_308_[i_309_];
                                int i_312_ = i_310_ + i_311_;
                                int i_313_ = ((i_310_ & 0xff00ff) + (i_311_ & 0xff00ff));
                                i_311_ = (i_313_ & 0x1000100) + (i_312_ - i_313_ & 0x10000);
                                is_308_[i_309_] = (~0xffffff | i_312_ - i_311_ | i_311_ - (i_311_ >>> 8));
                            }
                            f += f_172_;
                            f_173_ += f_174_;
                            f_175_ += f_176_;
                            f_177_ += f_178_;
                        } while (--i_169_ > 0);
                    } else {
                        int i_305_ = this.anInt1674;
                        int i_306_ = 256 - this.anInt1674;
                        do {
                            if (f < fs[++i]) {
                                i_168_ = ~0xffffff | ((int) f_173_ & 0xff0000 | (int) f_175_ & 0xff00 | (int) f_177_ & 0xff);
                                i_168_ = (((i_168_ & 0xff00ff) * i_306_ >> 8 & 0xff00ff) + ((i_168_ & 0xff00) * i_306_ >> 8 & 0xff00));
                                int i_307_ = is[i];
                                is[i] = (i_168_ + ((i_307_ & 0xff00ff) * i_305_ >> 8 & 0xff00ff) + ((i_307_ & 0xff00) * i_305_ >> 8 & 0xff00));
                            }
                            f += f_172_;
                            f_173_ += f_174_;
                            f_175_ += f_176_;
                            f_177_ += f_178_;
                        } while (--i_169_ > 0);
                    }
                }
            }
        }
    }

    final void method1022(float f, float f_314_, float f_315_, float f_316_, float f_317_, float f_318_, float f_319_, float f_320_, float f_321_, float f_322_, float f_323_, float f_324_) {
        if (aBoolean1675) {
            aHa_Sub1_1666.line((int) f, (int) f_316_, (int) f_317_, -8003, ItemSpriteCacheKey.HSV_TO_RGB[(int) f_322_], (int) f_314_);
            aHa_Sub1_1666.line((int) f_314_, (int) f_317_, (int) f_318_, -8003, ItemSpriteCacheKey.HSV_TO_RGB[(int) f_322_], (int) f_315_);
            aHa_Sub1_1666.line((int) f_315_, (int) f_318_, (int) f_316_, -8003, ItemSpriteCacheKey.HSV_TO_RGB[(int) f_322_], (int) f);
        } else {
            float f_325_ = f_317_ - f_316_;
            float f_326_ = f_314_ - f;
            float f_327_ = f_318_ - f_316_;
            float f_328_ = f_315_ - f;
            float f_329_ = f_323_ - f_322_;
            float f_330_ = f_324_ - f_322_;
            float f_331_ = f_320_ - f_319_;
            float f_332_ = f_321_ - f_319_;
            float f_333_;
            if (f_315_ != f_314_) f_333_ = (f_318_ - f_317_) / (f_315_ - f_314_);
            else f_333_ = 0.0F;
            float f_334_;
            if (f_314_ != f) f_334_ = f_325_ / f_326_;
            else f_334_ = 0.0F;
            float f_335_;
            if (f_315_ != f) f_335_ = f_327_ / f_328_;
            else f_335_ = 0.0F;
            float f_336_ = f_325_ * f_328_ - f_327_ * f_326_;
            if (f_336_ != 0.0F) {
                float f_337_ = (f_329_ * f_328_ - f_330_ * f_326_) / f_336_;
                float f_338_ = (f_330_ * f_325_ - f_329_ * f_327_) / f_336_;
                float f_339_ = (f_331_ * f_328_ - f_332_ * f_326_) / f_336_;
                float f_340_ = (f_332_ * f_325_ - f_331_ * f_327_) / f_336_;
                if (f <= f_314_ && f <= f_315_) {
                    if (!(f >= (float) this.height)) {
                        if (f_314_ > (float) this.height) f_314_ = (float) this.height;
                        if (f_315_ > (float) this.height) f_315_ = (float) this.height;
                        f_322_ = f_322_ - f_337_ * f_316_ + f_337_;
                        f_319_ = f_319_ - f_339_ * f_316_ + f_339_;
                        if (f_314_ < f_315_) {
                            f_318_ = f_316_;
                            if (f < 0.0F) {
                                f_318_ -= f_335_ * f;
                                f_316_ -= f_334_ * f;
                                f_322_ -= f_338_ * f;
                                f_319_ -= f_340_ * f;
                                f = 0.0F;
                            }
                            if (f_314_ < 0.0F) {
                                f_317_ -= f_333_ * f_314_;
                                f_314_ = 0.0F;
                            }
                            if (f != f_314_ && f_335_ < f_334_ || f == f_314_ && f_335_ > f_333_) {
                                f_315_ -= f_314_;
                                f_314_ -= f;
                                f = (float) (this.lineOffsets[(int) f]);
                                while (--f_314_ >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_318_, (int) f_316_, f_322_, f_337_, f_319_, f_339_);
                                    f_318_ += f_335_;
                                    f_316_ += f_334_;
                                    f_322_ += f_338_;
                                    f_319_ += f_340_;
                                    f += (float) anInt1678;
                                }
                                while (--f_315_ >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_318_, (int) f_317_, f_322_, f_337_, f_319_, f_339_);
                                    f_318_ += f_335_;
                                    f_317_ += f_333_;
                                    f_322_ += f_338_;
                                    f_319_ += f_340_;
                                    f += (float) anInt1678;
                                }
                            } else {
                                f_315_ -= f_314_;
                                f_314_ -= f;
                                f = (float) (this.lineOffsets[(int) f]);
                                while (--f_314_ >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_316_, (int) f_318_, f_322_, f_337_, f_319_, f_339_);
                                    f_318_ += f_335_;
                                    f_316_ += f_334_;
                                    f_322_ += f_338_;
                                    f_319_ += f_340_;
                                    f += (float) anInt1678;
                                }
                                while (--f_315_ >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_317_, (int) f_318_, f_322_, f_337_, f_319_, f_339_);
                                    f_318_ += f_335_;
                                    f_317_ += f_333_;
                                    f_322_ += f_338_;
                                    f_319_ += f_340_;
                                    f += (float) anInt1678;
                                }
                            }
                        } else {
                            f_317_ = f_316_;
                            if (f < 0.0F) {
                                f_317_ -= f_335_ * f;
                                f_316_ -= f_334_ * f;
                                f_322_ -= f_338_ * f;
                                f_319_ -= f_340_ * f;
                                f = 0.0F;
                            }
                            if (f_315_ < 0.0F) {
                                f_318_ -= f_333_ * f_315_;
                                f_315_ = 0.0F;
                            }
                            if (f != f_315_ && f_335_ < f_334_ || f == f_315_ && f_333_ > f_334_) {
                                f_314_ -= f_315_;
                                f_315_ -= f;
                                f = (float) (this.lineOffsets[(int) f]);
                                while (--f_315_ >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_317_, (int) f_316_, f_322_, f_337_, f_319_, f_339_);
                                    f_317_ += f_335_;
                                    f_316_ += f_334_;
                                    f_322_ += f_338_;
                                    f_319_ += f_340_;
                                    f += (float) anInt1678;
                                }
                                while (--f_314_ >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_318_, (int) f_316_, f_322_, f_337_, f_319_, f_339_);
                                    f_318_ += f_333_;
                                    f_316_ += f_334_;
                                    f_322_ += f_338_;
                                    f_319_ += f_340_;
                                    f += (float) anInt1678;
                                }
                            } else {
                                f_314_ -= f_315_;
                                f_315_ -= f;
                                f = (float) (this.lineOffsets[(int) f]);
                                while (--f_315_ >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_316_, (int) f_317_, f_322_, f_337_, f_319_, f_339_);
                                    f_317_ += f_335_;
                                    f_316_ += f_334_;
                                    f_322_ += f_338_;
                                    f_319_ += f_340_;
                                    f += (float) anInt1678;
                                }
                                while (--f_314_ >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_316_, (int) f_318_, f_322_, f_337_, f_319_, f_339_);
                                    f_318_ += f_333_;
                                    f_316_ += f_334_;
                                    f_322_ += f_338_;
                                    f_319_ += f_340_;
                                    f += (float) anInt1678;
                                }
                            }
                        }
                    }
                } else if (f_314_ <= f_315_) {
                    if (!(f_314_ >= (float) this.height)) {
                        if (f_315_ > (float) this.height) f_315_ = (float) this.height;
                        if (f > (float) this.height) f = (float) this.height;
                        f_323_ = f_323_ - f_337_ * f_317_ + f_337_;
                        f_320_ = f_320_ - f_339_ * f_317_ + f_339_;
                        if (f_315_ < f) {
                            f_316_ = f_317_;
                            if (f_314_ < 0.0F) {
                                f_316_ -= f_334_ * f_314_;
                                f_317_ -= f_333_ * f_314_;
                                f_323_ -= f_338_ * f_314_;
                                f_320_ -= f_340_ * f_314_;
                                f_314_ = 0.0F;
                            }
                            if (f_315_ < 0.0F) {
                                f_318_ -= f_335_ * f_315_;
                                f_315_ = 0.0F;
                            }
                            if (f_314_ != f_315_ && f_334_ < f_333_ || f_314_ == f_315_ && f_334_ > f_335_) {
                                f -= f_315_;
                                f_315_ -= f_314_;
                                f_314_ = (float) (this.lineOffsets[(int) f_314_]);
                                while (--f_315_ >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f_314_, 0, 0, (int) f_316_, (int) f_317_, f_323_, f_337_, f_320_, f_339_);
                                    f_316_ += f_334_;
                                    f_317_ += f_333_;
                                    f_323_ += f_338_;
                                    f_320_ += f_340_;
                                    f_314_ += (float) anInt1678;
                                }
                                while (--f >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f_314_, 0, 0, (int) f_316_, (int) f_318_, f_323_, f_337_, f_320_, f_339_);
                                    f_316_ += f_334_;
                                    f_318_ += f_335_;
                                    f_323_ += f_338_;
                                    f_320_ += f_340_;
                                    f_314_ += (float) anInt1678;
                                }
                            } else {
                                f -= f_315_;
                                f_315_ -= f_314_;
                                f_314_ = (float) (this.lineOffsets[(int) f_314_]);
                                while (--f_315_ >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f_314_, 0, 0, (int) f_317_, (int) f_316_, f_323_, f_337_, f_320_, f_339_);
                                    f_316_ += f_334_;
                                    f_317_ += f_333_;
                                    f_323_ += f_338_;
                                    f_320_ += f_340_;
                                    f_314_ += (float) anInt1678;
                                }
                                while (--f >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f_314_, 0, 0, (int) f_318_, (int) f_316_, f_323_, f_337_, f_320_, f_339_);
                                    f_316_ += f_334_;
                                    f_318_ += f_335_;
                                    f_323_ += f_338_;
                                    f_320_ += f_340_;
                                    f_314_ += (float) anInt1678;
                                }
                            }
                        } else {
                            f_318_ = f_317_;
                            if (f_314_ < 0.0F) {
                                f_318_ -= f_334_ * f_314_;
                                f_317_ -= f_333_ * f_314_;
                                f_323_ -= f_338_ * f_314_;
                                f_320_ -= f_340_ * f_314_;
                                f_314_ = 0.0F;
                            }
                            if (f < 0.0F) {
                                f_316_ -= f_335_ * f;
                                f = 0.0F;
                            }
                            if (f_334_ < f_333_) {
                                f_315_ -= f;
                                f -= f_314_;
                                f_314_ = (float) (this.lineOffsets[(int) f_314_]);
                                while (--f >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f_314_, 0, 0, (int) f_318_, (int) f_317_, f_323_, f_337_, f_320_, f_339_);
                                    f_318_ += f_334_;
                                    f_317_ += f_333_;
                                    f_323_ += f_338_;
                                    f_320_ += f_340_;
                                    f_314_ += (float) anInt1678;
                                }
                                while (--f_315_ >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f_314_, 0, 0, (int) f_316_, (int) f_317_, f_323_, f_337_, f_320_, f_339_);
                                    f_316_ += f_335_;
                                    f_317_ += f_333_;
                                    f_323_ += f_338_;
                                    f_320_ += f_340_;
                                    f_314_ += (float) anInt1678;
                                }
                            } else {
                                f_315_ -= f;
                                f -= f_314_;
                                f_314_ = (float) (this.lineOffsets[(int) f_314_]);
                                while (--f >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f_314_, 0, 0, (int) f_317_, (int) f_318_, f_323_, f_337_, f_320_, f_339_);
                                    f_318_ += f_334_;
                                    f_317_ += f_333_;
                                    f_323_ += f_338_;
                                    f_320_ += f_340_;
                                    f_314_ += (float) anInt1678;
                                }
                                while (--f_315_ >= 0.0F) {
                                    method1019(anIntArray1673, aFloatArray1677, (int) f_314_, 0, 0, (int) f_317_, (int) f_316_, f_323_, f_337_, f_320_, f_339_);
                                    f_316_ += f_335_;
                                    f_317_ += f_333_;
                                    f_323_ += f_338_;
                                    f_320_ += f_340_;
                                    f_314_ += (float) anInt1678;
                                }
                            }
                        }
                    }
                } else if (!(f_315_ >= (float) this.height)) {
                    if (f > (float) this.height) f = (float) this.height;
                    if (f_314_ > (float) this.height) f_314_ = (float) this.height;
                    f_324_ = f_324_ - f_337_ * f_318_ + f_337_;
                    f_321_ = f_321_ - f_339_ * f_318_ + f_339_;
                    if (f < f_314_) {
                        f_317_ = f_318_;
                        if (f_315_ < 0.0F) {
                            f_317_ -= f_333_ * f_315_;
                            f_318_ -= f_335_ * f_315_;
                            f_324_ -= f_338_ * f_315_;
                            f_321_ -= f_340_ * f_315_;
                            f_315_ = 0.0F;
                        }
                        if (f < 0.0F) {
                            f_316_ -= f_334_ * f;
                            f = 0.0F;
                        }
                        if (f_333_ < f_335_) {
                            f_314_ -= f;
                            f -= f_315_;
                            f_315_ = (float) (this.lineOffsets[(int) f_315_]);
                            while (--f >= 0.0F) {
                                method1019(anIntArray1673, aFloatArray1677, (int) f_315_, 0, 0, (int) f_317_, (int) f_318_, f_324_, f_337_, f_321_, f_339_);
                                f_317_ += f_333_;
                                f_318_ += f_335_;
                                f_324_ += f_338_;
                                f_321_ += f_340_;
                                f_315_ += (float) anInt1678;
                            }
                            while (--f_314_ >= 0.0F) {
                                method1019(anIntArray1673, aFloatArray1677, (int) f_315_, 0, 0, (int) f_317_, (int) f_316_, f_324_, f_337_, f_321_, f_339_);
                                f_317_ += f_333_;
                                f_316_ += f_334_;
                                f_324_ += f_338_;
                                f_321_ += f_340_;
                                f_315_ += (float) anInt1678;
                            }
                        } else {
                            f_314_ -= f;
                            f -= f_315_;
                            f_315_ = (float) (this.lineOffsets[(int) f_315_]);
                            while (--f >= 0.0F) {
                                method1019(anIntArray1673, aFloatArray1677, (int) f_315_, 0, 0, (int) f_318_, (int) f_317_, f_324_, f_337_, f_321_, f_339_);
                                f_317_ += f_333_;
                                f_318_ += f_335_;
                                f_324_ += f_338_;
                                f_321_ += f_340_;
                                f_315_ += (float) anInt1678;
                            }
                            while (--f_314_ >= 0.0F) {
                                method1019(anIntArray1673, aFloatArray1677, (int) f_315_, 0, 0, (int) f_316_, (int) f_317_, f_324_, f_337_, f_321_, f_339_);
                                f_317_ += f_333_;
                                f_316_ += f_334_;
                                f_324_ += f_338_;
                                f_321_ += f_340_;
                                f_315_ += (float) anInt1678;
                            }
                        }
                    } else {
                        f_316_ = f_318_;
                        if (f_315_ < 0.0F) {
                            f_316_ -= f_333_ * f_315_;
                            f_318_ -= f_335_ * f_315_;
                            f_324_ -= f_338_ * f_315_;
                            f_321_ -= f_340_ * f_315_;
                            f_315_ = 0.0F;
                        }
                        if (f_314_ < 0.0F) {
                            f_317_ -= f_334_ * f_314_;
                            f_314_ = 0.0F;
                        }
                        if (f_333_ < f_335_) {
                            f -= f_314_;
                            f_314_ -= f_315_;
                            f_315_ = (float) (this.lineOffsets[(int) f_315_]);
                            while (--f_314_ >= 0.0F) {
                                method1019(anIntArray1673, aFloatArray1677, (int) f_315_, 0, 0, (int) f_316_, (int) f_318_, f_324_, f_337_, f_321_, f_339_);
                                f_316_ += f_333_;
                                f_318_ += f_335_;
                                f_324_ += f_338_;
                                f_321_ += f_340_;
                                f_315_ += (float) anInt1678;
                            }
                            while (--f >= 0.0F) {
                                method1019(anIntArray1673, aFloatArray1677, (int) f_315_, 0, 0, (int) f_317_, (int) f_318_, f_324_, f_337_, f_321_, f_339_);
                                f_317_ += f_334_;
                                f_318_ += f_335_;
                                f_324_ += f_338_;
                                f_321_ += f_340_;
                                f_315_ += (float) anInt1678;
                            }
                        } else {
                            f -= f_314_;
                            f_314_ -= f_315_;
                            f_315_ = (float) (this.lineOffsets[(int) f_315_]);
                            while (--f_314_ >= 0.0F) {
                                method1019(anIntArray1673, aFloatArray1677, (int) f_315_, 0, 0, (int) f_318_, (int) f_316_, f_324_, f_337_, f_321_, f_339_);
                                f_316_ += f_333_;
                                f_318_ += f_335_;
                                f_324_ += f_338_;
                                f_321_ += f_340_;
                                f_315_ += (float) anInt1678;
                            }
                            while (--f >= 0.0F) {
                                method1019(anIntArray1673, aFloatArray1677, (int) f_315_, 0, 0, (int) f_318_, (int) f_317_, f_324_, f_337_, f_321_, f_339_);
                                f_317_ += f_334_;
                                f_318_ += f_335_;
                                f_324_ += f_338_;
                                f_321_ += f_340_;
                                f_315_ += (float) anInt1678;
                            }
                        }
                    }
                }
            }
        }
    }

    final void method1023(boolean bool) {
        aBoolean1675 = bool;
    }

    /* NOTE: originally calls method1027(...) behind "if (anIntArray1698 ==
     * null)" (a texture-lookup-miss branch). method1027 is not in the
     * genuine-methods list (never observed covered), so it is treated as
     * dead code and omitted. Flagged for a follow-up compile-fix pass. */
    final void method1024(float f, float f_341_, float f_342_, float f_343_, float f_344_, float f_345_, float f_346_, float f_347_, float f_348_, float f_349_, float f_350_, float f_351_, float f_352_, float f_353_, float f_354_, int i, int i_355_, int i_356_, int i_357_, int i_358_, int i_359_, int i_360_, int i_361_) {
        if (i_361_ != anInt1697) {
            anIntArray1698 = aHa_Sub1_1666.method3719(i_361_);
            if (anIntArray1698 == null) {
                method1027((float) (int) f, (float) (int) f_341_, (float) (int) f_342_, (float) (int) f_343_, (float) (int) f_344_, (float) (int) f_345_, (float) (int) f_346_, (float) (int) f_347_, (float) (int) f_348_, JavaBillboardFace.method206(i, i_357_ | i_358_ << 24, 255), JavaBillboardFace.method206(i_355_, i_357_ | i_359_ << 24, 255), JavaBillboardFace.method206(i_356_, i_357_ | i_360_ << 24, 255));
                return;
            }
            anInt1693 = (aHa_Sub1_1666.method3727(i_361_) ? 64 : aHa_Sub1_1666.anInt7501);
            anInt1690 = anInt1693 - 1;
            anInt1683 = aHa_Sub1_1666.method3726(i_361_);
            aBoolean1694 = aHa_Sub1_1666.method3714(i_361_);
        }
        anInt1696 = i_357_;
        float f_362_ = (float) (i >> 24 & 0xff);
        float f_363_ = (float) (i_355_ >> 24 & 0xff);
        float f_364_ = (float) (i_356_ >> 24 & 0xff);
        float f_365_ = (float) (i >> 16 & 0xff);
        float f_366_ = (float) (i_355_ >> 16 & 0xff);
        float f_367_ = (float) (i_356_ >> 16 & 0xff);
        float f_368_ = (float) (i >> 8 & 0xff);
        float f_369_ = (float) (i_355_ >> 8 & 0xff);
        float f_370_ = (float) (i_356_ >> 8 & 0xff);
        float f_371_ = (float) (i & 0xff);
        float f_372_ = (float) (i_355_ & 0xff);
        float f_373_ = (float) (i_356_ & 0xff);
        f_349_ /= f_346_;
        f_350_ /= f_347_;
        f_351_ /= f_348_;
        f_352_ /= f_346_;
        f_353_ /= f_347_;
        f_354_ /= f_348_;
        f_346_ = 1.0F / f_346_;
        f_347_ = 1.0F / f_347_;
        f_348_ = 1.0F / f_348_;
        float f_374_ = 0.0F;
        float f_375_ = 0.0F;
        float f_376_ = 0.0F;
        float f_377_ = 0.0F;
        float f_378_ = 0.0F;
        float f_379_ = 0.0F;
        float f_380_ = 0.0F;
        float f_381_ = 0.0F;
        float f_382_ = 0.0F;
        if (f_341_ != f) {
            float f_383_ = f_341_ - f;
            f_374_ = (f_344_ - f_343_) / f_383_;
            f_375_ = (f_347_ - f_346_) / f_383_;
            f_376_ = (f_350_ - f_349_) / f_383_;
            f_377_ = (f_353_ - f_352_) / f_383_;
            f_378_ = (float) (i_359_ - i_358_) / f_383_;
            f_379_ = (f_363_ - f_362_) / f_383_;
            f_380_ = (f_366_ - f_365_) / f_383_;
            f_381_ = (f_369_ - f_368_) / f_383_;
            f_382_ = (f_372_ - f_371_) / f_383_;
        }
        float f_384_ = 0.0F;
        float f_385_ = 0.0F;
        float f_386_ = 0.0F;
        float f_387_ = 0.0F;
        float f_388_ = 0.0F;
        float f_389_ = 0.0F;
        float f_390_ = 0.0F;
        float f_391_ = 0.0F;
        float f_392_ = 0.0F;
        if (f_342_ != f_341_) {
            float f_393_ = f_342_ - f_341_;
            f_384_ = (f_345_ - f_344_) / f_393_;
            f_385_ = (f_348_ - f_347_) / f_393_;
            f_386_ = (f_351_ - f_350_) / f_393_;
            f_387_ = (f_354_ - f_353_) / f_393_;
            f_388_ = (float) (i_360_ - i_359_) / f_393_;
            f_389_ = (f_364_ - f_363_) / f_393_;
            f_390_ = (f_367_ - f_366_) / f_393_;
            f_391_ = (f_370_ - f_369_) / f_393_;
            f_392_ = (f_373_ - f_372_) / f_393_;
        }
        float f_394_ = 0.0F;
        float f_395_ = 0.0F;
        float f_396_ = 0.0F;
        float f_397_ = 0.0F;
        float f_398_ = 0.0F;
        float f_399_ = 0.0F;
        float f_400_ = 0.0F;
        float f_401_ = 0.0F;
        float f_402_ = 0.0F;
        if (f != f_342_) {
            float f_403_ = f - f_342_;
            f_394_ = (f_343_ - f_345_) / f_403_;
            f_395_ = (f_346_ - f_348_) / f_403_;
            f_396_ = (f_349_ - f_351_) / f_403_;
            f_397_ = (f_352_ - f_354_) / f_403_;
            f_398_ = (float) (i_358_ - i_360_) / f_403_;
            f_399_ = (f_362_ - f_364_) / f_403_;
            f_400_ = (f_365_ - f_367_) / f_403_;
            f_401_ = (f_368_ - f_370_) / f_403_;
            f_402_ = (f_371_ - f_373_) / f_403_;
        }
        if (f <= f_341_ && f <= f_342_) {
            if (!(f >= (float) this.height)) {
                if (f_341_ > (float) this.height) f_341_ = (float) this.height;
                if (f_342_ > (float) this.height) f_342_ = (float) this.height;
                if (f_341_ < f_342_) {
                    f_345_ = f_343_;
                    f_348_ = f_346_;
                    f_351_ = f_349_;
                    f_354_ = f_352_;
                    i_360_ = i_358_;
                    f_364_ = f_362_;
                    f_367_ = f_365_;
                    f_370_ = f_368_;
                    f_373_ = f_371_;
                    if (f < 0.0F) {
                        f_343_ -= f_374_ * f;
                        f_345_ -= f_394_ * f;
                        f_346_ -= f_375_ * f;
                        f_348_ -= f_395_ * f;
                        f_349_ -= f_376_ * f;
                        f_351_ -= f_396_ * f;
                        f_352_ -= f_377_ * f;
                        f_354_ -= f_397_ * f;
                        i_358_ -= f_378_ * f;
                        i_360_ -= f_398_ * f;
                        f_362_ -= f_379_ * f;
                        f_364_ -= f_399_ * f;
                        f_365_ -= f_379_ * f;
                        f_367_ -= f_399_ * f;
                        f_368_ -= f_379_ * f;
                        f_370_ -= f_399_ * f;
                        f_371_ -= f_379_ * f;
                        f_373_ -= f_399_ * f;
                        f = 0.0F;
                    }
                    if (f_341_ < 0.0F) {
                        f_344_ -= f_384_ * f_341_;
                        f_347_ -= f_385_ * f_341_;
                        f_350_ -= f_386_ * f_341_;
                        f_353_ -= f_387_ * f_341_;
                        i_359_ -= f_388_ * f_341_;
                        f_363_ -= f_389_ * f_341_;
                        f_366_ -= f_390_ * f_341_;
                        f_369_ -= f_391_ * f_341_;
                        f_372_ -= f_392_ * f_341_;
                        f_341_ = 0.0F;
                    }
                    if (f != f_341_ && f_394_ < f_374_ || f == f_341_ && f_394_ > f_384_) {
                        f_342_ -= f_341_;
                        f_341_ -= f;
                        f = (float) this.lineOffsets[(int) f];
                        while (--f_341_ >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f, (int) f_345_, (int) f_343_, f_348_, f_346_, f_351_, f_349_, f_354_, f_352_, (float) i_360_, (float) i_358_, f_364_, f_362_, f_367_, f_365_, f_370_, f_368_, f_373_, f_371_);
                            f_343_ += f_374_;
                            f_345_ += f_394_;
                            f_346_ += f_375_;
                            f_348_ += f_395_;
                            f_349_ += f_376_;
                            f_351_ += f_396_;
                            f_352_ += f_377_;
                            f_354_ += f_397_;
                            i_358_ += f_378_;
                            i_360_ += f_398_;
                            f_362_ += f_379_;
                            f_364_ += f_399_;
                            f_365_ += f_380_;
                            f_367_ += f_400_;
                            f_368_ += f_381_;
                            f_370_ += f_401_;
                            f_371_ += f_382_;
                            f_373_ += f_402_;
                            f += (float) anInt1678;
                        }
                        while (--f_342_ >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f, (int) f_345_, (int) f_344_, f_348_, f_347_, f_351_, f_350_, f_354_, f_353_, (float) i_360_, (float) i_359_, f_364_, f_363_, f_367_, f_366_, f_370_, f_369_, f_373_, f_372_);
                            f_344_ += f_384_;
                            f_345_ += f_394_;
                            f_347_ += f_385_;
                            f_348_ += f_395_;
                            f_350_ += f_386_;
                            f_351_ += f_396_;
                            f_353_ += f_387_;
                            f_354_ += f_397_;
                            i_359_ += f_388_;
                            i_360_ += f_398_;
                            f_363_ += f_389_;
                            f_364_ += f_399_;
                            f_366_ += f_390_;
                            f_367_ += f_400_;
                            f_369_ += f_391_;
                            f_370_ += f_401_;
                            f_372_ += f_392_;
                            f_373_ += f_402_;
                            f += (float) anInt1678;
                        }
                    } else {
                        f_342_ -= f_341_;
                        f_341_ -= f;
                        f = (float) this.lineOffsets[(int) f];
                        while (--f_341_ >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f, (int) f_343_, (int) f_345_, f_346_, f_348_, f_349_, f_351_, f_352_, f_354_, (float) i_358_, (float) i_360_, f_362_, f_364_, f_365_, f_367_, f_368_, f_370_, f_371_, f_373_);
                            f_343_ += f_374_;
                            f_345_ += f_394_;
                            f_346_ += f_375_;
                            f_348_ += f_395_;
                            f_349_ += f_376_;
                            f_351_ += f_396_;
                            f_352_ += f_377_;
                            f_354_ += f_397_;
                            i_358_ += f_378_;
                            i_360_ += f_398_;
                            f_362_ += f_379_;
                            f_364_ += f_399_;
                            f_365_ += f_380_;
                            f_367_ += f_400_;
                            f_368_ += f_381_;
                            f_370_ += f_401_;
                            f_371_ += f_382_;
                            f_373_ += f_402_;
                            f += (float) anInt1678;
                        }
                        while (--f_342_ >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f, (int) f_344_, (int) f_345_, f_347_, f_348_, f_350_, f_351_, f_353_, f_354_, (float) i_359_, (float) i_360_, f_363_, f_364_, f_366_, f_367_, f_369_, f_370_, f_372_, f_373_);
                            f_344_ += f_384_;
                            f_345_ += f_394_;
                            f_347_ += f_385_;
                            f_348_ += f_395_;
                            f_350_ += f_386_;
                            f_351_ += f_396_;
                            f_353_ += f_387_;
                            f_354_ += f_397_;
                            i_359_ += f_388_;
                            i_360_ += f_398_;
                            f_363_ += f_389_;
                            f_364_ += f_399_;
                            f_366_ += f_390_;
                            f_367_ += f_400_;
                            f_369_ += f_391_;
                            f_370_ += f_401_;
                            f_372_ += f_392_;
                            f_373_ += f_402_;
                            f += (float) anInt1678;
                        }
                    }
                } else {
                    f_344_ = f_343_;
                    f_347_ = f_346_;
                    f_350_ = f_349_;
                    f_353_ = f_352_;
                    i_359_ = i_358_;
                    f_363_ = f_362_;
                    f_366_ = f_365_;
                    f_369_ = f_368_;
                    f_372_ = f_371_;
                    if (f < 0.0F) {
                        f_343_ -= f_374_ * f;
                        f_344_ -= f_394_ * f;
                        f_346_ -= f_375_ * f;
                        f_347_ -= f_395_ * f;
                        f_349_ -= f_376_ * f;
                        f_350_ -= f_396_ * f;
                        f_352_ -= f_377_ * f;
                        f_353_ -= f_397_ * f;
                        i_358_ -= f_378_ * f;
                        i_359_ -= f_398_ * f;
                        f_362_ -= f_379_ * f;
                        f_363_ -= f_399_ * f;
                        f_365_ -= f_379_ * f;
                        f_366_ -= f_399_ * f;
                        f_368_ -= f_379_ * f;
                        f_369_ -= f_399_ * f;
                        f_371_ -= f_379_ * f;
                        f_372_ -= f_399_ * f;
                        f = 0.0F;
                    }
                    if (f_342_ < 0.0F) {
                        f_345_ -= f_384_ * f_342_;
                        f_348_ -= f_385_ * f_342_;
                        f_351_ -= f_386_ * f_342_;
                        f_354_ -= f_387_ * f_342_;
                        i_360_ -= f_388_ * f_342_;
                        f_364_ -= f_389_ * f_342_;
                        f_367_ -= f_390_ * f_342_;
                        f_370_ -= f_391_ * f_342_;
                        f_373_ -= f_392_ * f_342_;
                        f_342_ = 0.0F;
                    }
                    if (f != f_342_ && f_394_ < f_374_ || f == f_342_ && f_384_ > f_374_) {
                        f_341_ -= f_342_;
                        f_342_ -= f;
                        f = (float) this.lineOffsets[(int) f];
                        while (--f_342_ >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f, (int) f_344_, (int) f_343_, f_347_, f_346_, f_350_, f_349_, f_353_, f_352_, (float) i_359_, (float) i_358_, f_363_, f_362_, f_366_, f_365_, f_369_, f_368_, f_372_, f_371_);
                            f_343_ += f_374_;
                            f_344_ += f_394_;
                            f_346_ += f_375_;
                            f_347_ += f_395_;
                            f_349_ += f_376_;
                            f_350_ += f_396_;
                            f_352_ += f_377_;
                            f_353_ += f_397_;
                            i_358_ += f_378_;
                            i_359_ += f_398_;
                            f_362_ += f_379_;
                            f_363_ += f_399_;
                            f_365_ += f_380_;
                            f_366_ += f_400_;
                            f_368_ += f_381_;
                            f_369_ += f_401_;
                            f_371_ += f_382_;
                            f_372_ += f_402_;
                            f += (float) anInt1678;
                        }
                        while (--f_341_ >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f, (int) f_345_, (int) f_343_, f_348_, f_346_, f_351_, f_349_, f_354_, f_352_, (float) i_360_, (float) i_358_, f_364_, f_362_, f_367_, f_365_, f_370_, f_368_, f_373_, f_371_);
                            f_345_ += f_384_;
                            f_343_ += f_374_;
                            f_348_ += f_385_;
                            f_346_ += f_375_;
                            f_351_ += f_386_;
                            f_349_ += f_376_;
                            f_354_ += f_387_;
                            f_352_ += f_377_;
                            i_360_ += f_388_;
                            i_358_ += f_378_;
                            f_364_ += f_389_;
                            f_362_ += f_379_;
                            f_367_ += f_390_;
                            f_365_ += f_380_;
                            f_370_ += f_391_;
                            f_368_ += f_381_;
                            f_373_ += f_392_;
                            f_371_ += f_382_;
                            f += (float) anInt1678;
                        }
                    } else {
                        f_341_ -= f_342_;
                        f_342_ -= f;
                        f = (float) this.lineOffsets[(int) f];
                        while (--f_342_ >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f, (int) f_343_, (int) f_344_, f_346_, f_347_, f_349_, f_350_, f_352_, f_353_, (float) i_358_, (float) i_359_, f_362_, f_363_, f_365_, f_366_, f_368_, f_369_, f_371_, f_372_);
                            f_344_ += f_394_;
                            f_343_ += f_374_;
                            f_347_ += f_395_;
                            f_346_ += f_375_;
                            f_350_ += f_396_;
                            f_349_ += f_376_;
                            f_353_ += f_397_;
                            f_352_ += f_377_;
                            i_359_ += f_398_;
                            i_358_ += f_378_;
                            f_363_ += f_399_;
                            f_362_ += f_379_;
                            f_366_ += f_400_;
                            f_365_ += f_380_;
                            f_369_ += f_401_;
                            f_368_ += f_381_;
                            f_372_ += f_402_;
                            f_371_ += f_382_;
                            f += (float) anInt1678;
                        }
                        while (--f_341_ >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f, (int) f_343_, (int) f_345_, f_346_, f_348_, f_349_, f_351_, f_352_, f_354_, (float) i_358_, (float) i_360_, f_362_, f_364_, f_365_, f_367_, f_368_, f_370_, f_371_, f_373_);
                            f_343_ += f_374_;
                            f_345_ += f_384_;
                            f_346_ += f_375_;
                            f_348_ += f_385_;
                            f_349_ += f_376_;
                            f_351_ += f_386_;
                            f_352_ += f_377_;
                            f_354_ += f_397_;
                            i_358_ += f_378_;
                            i_360_ += f_388_;
                            f_362_ += f_379_;
                            f_364_ += f_389_;
                            f_365_ += f_380_;
                            f_367_ += f_390_;
                            f_368_ += f_381_;
                            f_370_ += f_391_;
                            f_371_ += f_382_;
                            f_373_ += f_392_;
                            f += (float) anInt1678;
                        }
                    }
                }
            }
        } else if (f_341_ <= f_342_) {
            if (!(f_341_ >= (float) this.height)) {
                if (f_342_ > (float) this.height) f_342_ = (float) this.height;
                if (f > (float) this.height) f = (float) this.height;
                if (f_342_ < f) {
                    f_343_ = f_344_;
                    f_346_ = f_347_;
                    f_349_ = f_350_;
                    f_352_ = f_353_;
                    i_358_ = i_359_;
                    f_362_ = f_363_;
                    f_365_ = f_366_;
                    f_368_ = f_369_;
                    f_371_ = f_372_;
                    if (f_341_ < 0.0F) {
                        f_343_ -= f_374_ * f_341_;
                        f_344_ -= f_384_ * f_341_;
                        f_346_ -= f_375_ * f_341_;
                        f_347_ -= f_385_ * f_341_;
                        f_349_ -= f_376_ * f_341_;
                        f_350_ -= f_386_ * f_341_;
                        f_352_ -= f_377_ * f_341_;
                        f_353_ -= f_387_ * f_341_;
                        i_358_ -= f_378_ * f_341_;
                        i_359_ -= f_388_ * f_341_;
                        f_362_ -= f_379_ * f_341_;
                        f_363_ -= f_389_ * f_341_;
                        f_365_ -= f_380_ * f_341_;
                        f_366_ -= f_390_ * f_341_;
                        f_368_ -= f_381_ * f_341_;
                        f_369_ -= f_391_ * f_341_;
                        f_371_ -= f_382_ * f_341_;
                        f_372_ -= f_392_ * f_341_;
                        f_341_ = 0.0F;
                    }
                    if (f_342_ < 0.0F) {
                        f_345_ -= f_394_ * f_342_;
                        f_348_ -= f_395_ * f_342_;
                        f_351_ -= f_396_ * f_342_;
                        f_354_ -= f_397_ * f_342_;
                        i_360_ -= f_398_ * f_342_;
                        f_364_ -= f_399_ * f_342_;
                        f_367_ -= f_400_ * f_342_;
                        f_370_ -= f_401_ * f_342_;
                        f_373_ -= f_402_ * f_342_;
                        f_342_ = 0.0F;
                    }
                    if (f_341_ != f_342_ && f_374_ < f_384_ || f_341_ == f_342_ && f_374_ > f_394_) {
                        f -= f_342_;
                        f_342_ -= f_341_;
                        f_341_ = (float) (this.lineOffsets[(int) f_341_]);
                        while (--f_342_ >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f_341_, (int) f_343_, (int) f_344_, f_346_, f_347_, f_349_, f_350_, f_352_, f_353_, (float) i_358_, (float) i_359_, f_362_, f_363_, f_365_, f_366_, f_368_, f_369_, f_371_, f_372_);
                            f_343_ += f_374_;
                            f_344_ += f_384_;
                            f_346_ += f_375_;
                            f_347_ += f_385_;
                            f_349_ += f_376_;
                            f_350_ += f_386_;
                            f_352_ += f_377_;
                            f_353_ += f_387_;
                            i_358_ += f_378_;
                            i_359_ += f_388_;
                            f_362_ += f_379_;
                            f_363_ += f_389_;
                            f_365_ += f_380_;
                            f_366_ += f_390_;
                            f_368_ += f_381_;
                            f_369_ += f_391_;
                            f_371_ += f_382_;
                            f_372_ += f_392_;
                            f_341_ += (float) anInt1678;
                        }
                        while (--f >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f_341_, (int) f_343_, (int) f_345_, f_346_, f_348_, f_349_, f_351_, f_352_, f_354_, (float) i_358_, (float) i_360_, f_362_, f_364_, f_365_, f_367_, f_368_, f_370_, f_371_, f_373_);
                            f_343_ += f_374_;
                            f_345_ += f_394_;
                            f_346_ += f_375_;
                            f_348_ += f_395_;
                            f_349_ += f_376_;
                            f_351_ += f_396_;
                            f_352_ += f_377_;
                            f_354_ += f_397_;
                            i_358_ += f_378_;
                            i_360_ += f_398_;
                            f_362_ += f_379_;
                            f_364_ += f_399_;
                            f_365_ += f_380_;
                            f_367_ += f_400_;
                            f_368_ += f_381_;
                            f_370_ += f_401_;
                            f_371_ += f_382_;
                            f_373_ += f_402_;
                            f_341_ += (float) anInt1678;
                        }
                    } else {
                        f -= f_342_;
                        f_342_ -= f_341_;
                        f_341_ = (float) (this.lineOffsets[(int) f_341_]);
                        while (--f_342_ >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f_341_, (int) f_344_, (int) f_343_, f_347_, f_346_, f_350_, f_349_, f_353_, f_352_, (float) i_359_, (float) i_358_, f_363_, f_362_, f_366_, f_365_, f_369_, f_368_, f_372_, f_371_);
                            f_344_ += f_384_;
                            f_343_ += f_374_;
                            f_347_ += f_385_;
                            f_346_ += f_375_;
                            f_350_ += f_386_;
                            f_349_ += f_376_;
                            f_353_ += f_387_;
                            f_352_ += f_377_;
                            i_359_ += f_388_;
                            i_358_ += f_378_;
                            f_363_ += f_389_;
                            f_362_ += f_379_;
                            f_366_ += f_390_;
                            f_365_ += f_380_;
                            f_369_ += f_391_;
                            f_368_ += f_381_;
                            f_372_ += f_392_;
                            f_371_ += f_382_;
                            f_341_ += (float) anInt1678;
                        }
                        while (--f >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f_341_, (int) f_345_, (int) f_343_, f_348_, f_346_, f_351_, f_349_, f_354_, f_352_, (float) i_360_, (float) i_358_, f_364_, f_362_, f_367_, f_365_, f_370_, f_368_, f_373_, f_371_);
                            f_345_ += f_394_;
                            f_343_ += f_374_;
                            f_348_ += f_395_;
                            f_346_ += f_375_;
                            f_351_ += f_396_;
                            f_349_ += f_376_;
                            f_354_ += f_397_;
                            f_352_ += f_377_;
                            i_360_ += f_398_;
                            i_358_ += f_378_;
                            f_364_ += f_399_;
                            f_362_ += f_379_;
                            f_367_ += f_400_;
                            f_365_ += f_380_;
                            f_370_ += f_401_;
                            f_368_ += f_381_;
                            f_373_ += f_402_;
                            f_371_ += f_382_;
                            f_341_ += (float) anInt1678;
                        }
                    }
                } else {
                    f_345_ = f_344_;
                    f_348_ = f_347_;
                    f_351_ = f_350_;
                    f_354_ = f_353_;
                    i_360_ = i_359_;
                    f_364_ = f_363_;
                    f_367_ = f_366_;
                    f_370_ = f_369_;
                    f_373_ = f_372_;
                    if (f_341_ < 0.0F) {
                        f_345_ -= f_374_ * f_341_;
                        f_344_ -= f_384_ * f_341_;
                        f_348_ -= f_375_ * f_341_;
                        f_347_ -= f_385_ * f_341_;
                        f_351_ -= f_376_ * f_341_;
                        f_350_ -= f_386_ * f_341_;
                        f_354_ -= f_377_ * f_341_;
                        f_353_ -= f_387_ * f_341_;
                        i_360_ -= f_378_ * f_341_;
                        i_359_ -= f_388_ * f_341_;
                        f_364_ -= f_379_ * f_341_;
                        f_363_ -= f_389_ * f_341_;
                        f_367_ -= f_380_ * f_341_;
                        f_366_ -= f_390_ * f_341_;
                        f_370_ -= f_381_ * f_341_;
                        f_369_ -= f_391_ * f_341_;
                        f_373_ -= f_382_ * f_341_;
                        f_372_ -= f_392_ * f_341_;
                        f_341_ = 0.0F;
                    }
                    if (f < 0.0F) {
                        f_343_ -= f_394_ * f;
                        f_346_ -= f_395_ * f;
                        f_349_ -= f_396_ * f;
                        f_352_ -= f_397_ * f;
                        i_358_ -= f_398_ * f;
                        f_362_ -= f_399_ * f;
                        f_365_ -= f_400_ * f;
                        f_368_ -= f_401_ * f;
                        f_371_ -= f_402_ * f;
                        f = 0.0F;
                    }
                    f_342_ -= f;
                    f -= f_341_;
                    f_341_ = (float) (this.lineOffsets[(int) f_341_]);
                    if (f_374_ < f_384_) {
                        while (--f >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f_341_, (int) f_345_, (int) f_344_, f_348_, f_347_, f_351_, f_350_, f_354_, f_353_, (float) i_360_, (float) i_359_, f_364_, f_363_, f_367_, f_366_, f_370_, f_369_, f_373_, f_372_);
                            f_345_ += f_374_;
                            f_344_ += f_384_;
                            f_348_ += f_375_;
                            f_347_ += f_385_;
                            f_351_ += f_376_;
                            f_350_ += f_386_;
                            f_354_ += f_377_;
                            f_353_ += f_387_;
                            i_360_ += f_378_;
                            i_359_ += f_388_;
                            f_364_ += f_379_;
                            f_363_ += f_389_;
                            f_367_ += f_380_;
                            f_366_ += f_390_;
                            f_370_ += f_381_;
                            f_369_ += f_391_;
                            f_373_ += f_382_;
                            f_372_ += f_392_;
                            f_341_ += (float) anInt1678;
                        }
                        while (--f_342_ >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f_341_, (int) f_343_, (int) f_344_, f_346_, f_347_, f_349_, f_350_, f_352_, f_353_, (float) i_358_, (float) i_359_, f_362_, f_363_, f_365_, f_366_, f_368_, f_369_, f_371_, f_372_);
                            f_343_ += f_394_;
                            f_344_ += f_384_;
                            f_346_ += f_395_;
                            f_347_ += f_385_;
                            f_349_ += f_396_;
                            f_350_ += f_386_;
                            f_352_ += f_397_;
                            f_353_ += f_387_;
                            i_358_ += f_398_;
                            i_359_ += f_388_;
                            f_362_ += f_399_;
                            f_363_ += f_389_;
                            f_365_ += f_400_;
                            f_366_ += f_390_;
                            f_368_ += f_401_;
                            f_369_ += f_391_;
                            f_371_ += f_402_;
                            f_372_ += f_392_;
                            f_341_ += (float) anInt1678;
                        }
                    } else {
                        while (--f >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f_341_, (int) f_344_, (int) f_345_, f_347_, f_348_, f_350_, f_351_, f_353_, f_354_, (float) i_359_, (float) i_360_, f_363_, f_364_, f_366_, f_367_, f_369_, f_370_, f_372_, f_373_);
                            f_344_ += f_384_;
                            f_345_ += f_374_;
                            f_347_ += f_385_;
                            f_348_ += f_375_;
                            f_350_ += f_386_;
                            f_351_ += f_376_;
                            f_353_ += f_387_;
                            f_354_ += f_377_;
                            i_359_ += f_388_;
                            i_360_ += f_378_;
                            f_363_ += f_389_;
                            f_364_ += f_379_;
                            f_366_ += f_390_;
                            f_367_ += f_380_;
                            f_369_ += f_391_;
                            f_370_ += f_381_;
                            f_372_ += f_392_;
                            f_373_ += f_382_;
                            f_341_ += (float) anInt1678;
                        }
                        while (--f_342_ >= 0.0F) {
                            method1016(anIntArray1673, anIntArray1698, (int) f_341_, (int) f_344_, (int) f_343_, f_347_, f_346_, f_350_, f_349_, f_353_, f_352_, (float) i_359_, (float) i_358_, f_363_, f_362_, f_366_, f_365_, f_369_, f_368_, f_372_, f_371_);
                            f_344_ += f_384_;
                            f_343_ += f_394_;
                            f_347_ += f_385_;
                            f_346_ += f_395_;
                            f_350_ += f_386_;
                            f_349_ += f_396_;
                            f_353_ += f_387_;
                            f_352_ += f_397_;
                            i_359_ += f_388_;
                            i_358_ += f_398_;
                            f_363_ += f_389_;
                            f_362_ += f_399_;
                            f_366_ += f_390_;
                            f_365_ += f_400_;
                            f_369_ += f_391_;
                            f_368_ += f_401_;
                            f_372_ += f_392_;
                            f_371_ += f_402_;
                            f_341_ += (float) anInt1678;
                        }
                    }
                }
            }
        } else if (!(f_342_ >= (float) this.height)) {
            if (f > (float) this.height) f = (float) this.height;
            if (f_341_ > (float) this.height) f_341_ = (float) this.height;
            if (f < f_341_) {
                f_344_ = f_345_;
                f_347_ = f_348_;
                f_350_ = f_351_;
                f_353_ = f_354_;
                i_359_ = i_360_;
                f_363_ = f_364_;
                f_366_ = f_367_;
                f_369_ = f_370_;
                f_372_ = f_373_;
                if (f_342_ < 0.0F) {
                    f_345_ -= f_394_ * f_342_;
                    f_344_ -= f_384_ * f_342_;
                    f_348_ -= f_395_ * f_342_;
                    f_347_ -= f_385_ * f_342_;
                    f_351_ -= f_396_ * f_342_;
                    f_350_ -= f_386_ * f_342_;
                    f_354_ -= f_397_ * f_342_;
                    f_353_ -= f_387_ * f_342_;
                    i_360_ -= f_398_ * 3.0F;
                    i_359_ -= f_388_ * f_342_;
                    f_364_ -= f_399_ * f_342_;
                    f_363_ -= f_389_ * f_342_;
                    f_367_ -= f_400_ * f_342_;
                    f_366_ -= f_390_ * f_342_;
                    f_370_ -= f_401_ * f_342_;
                    f_369_ -= f_391_ * f_342_;
                    f_373_ -= f_402_ * f_342_;
                    f_372_ -= f_392_ * f_342_;
                    f_342_ = 0.0F;
                }
                if (f < 0.0F) {
                    f_343_ -= f_374_ * f;
                    f_346_ -= f_375_ * f;
                    f_349_ -= f_376_ * f;
                    f_352_ -= f_377_ * f;
                    i_358_ -= f_378_ * f;
                    f_362_ -= f_379_ * f;
                    f_365_ -= f_380_ * f;
                    f_368_ -= f_381_ * f;
                    f_371_ -= f_382_ * f;
                    f = 0.0F;
                }
                if (f_384_ < f_394_) {
                    f_341_ -= f;
                    f -= f_342_;
                    f_342_ = (float) (this.lineOffsets[(int) f_342_]);
                    while (--f >= 0.0F) {
                        method1016(anIntArray1673, anIntArray1698, (int) f_342_, (int) f_344_, (int) f_345_, f_347_, f_348_, f_350_, f_351_, f_353_, f_354_, (float) i_359_, (float) i_360_, f_363_, f_364_, f_366_, f_367_, f_369_, f_370_, f_372_, f_373_);
                        f_344_ += f_384_;
                        f_345_ += f_394_;
                        f_347_ += f_385_;
                        f_348_ += f_395_;
                        f_350_ += f_386_;
                        f_351_ += f_396_;
                        f_353_ += f_387_;
                        f_354_ += f_397_;
                        i_359_ += f_388_;
                        i_360_ += f_398_;
                        f_363_ += f_389_;
                        f_364_ += f_399_;
                        f_366_ += f_390_;
                        f_367_ += f_400_;
                        f_369_ += f_391_;
                        f_370_ += f_401_;
                        f_372_ += f_392_;
                        f_373_ += f_402_;
                        f_342_ += (float) anInt1678;
                    }
                    while (--f_341_ >= 0.0F) {
                        method1016(anIntArray1673, anIntArray1698, (int) f_342_, (int) f_344_, (int) f_343_, f_347_, f_346_, f_350_, f_349_, f_353_, f_352_, (float) i_359_, (float) i_358_, f_363_, f_362_, f_366_, f_365_, f_369_, f_368_, f_372_, f_371_);
                        f_344_ += f_384_;
                        f_343_ += f_374_;
                        f_347_ += f_385_;
                        f_346_ += f_375_;
                        f_350_ += f_386_;
                        f_349_ += f_376_;
                        f_353_ += f_387_;
                        f_352_ += f_377_;
                        i_359_ += f_388_;
                        i_358_ += f_378_;
                        f_363_ += f_389_;
                        f_362_ += f_379_;
                        f_366_ += f_390_;
                        f_365_ += f_380_;
                        f_369_ += f_391_;
                        f_368_ += f_381_;
                        f_372_ += f_392_;
                        f_371_ += f_382_;
                        f_342_ += (float) anInt1678;
                    }
                } else {
                    f_341_ -= f;
                    f -= f_342_;
                    f_342_ = (float) (this.lineOffsets[(int) f_342_]);
                    while (--f >= 0.0F) {
                        method1016(anIntArray1673, anIntArray1698, (int) f_342_, (int) f_345_, (int) f_344_, f_348_, f_347_, f_351_, f_350_, f_354_, f_353_, (float) i_360_, (float) i_359_, f_364_, f_363_, f_367_, f_366_, f_370_, f_369_, f_373_, f_372_);
                        f_345_ += f_394_;
                        f_344_ += f_384_;
                        f_348_ += f_395_;
                        f_347_ += f_385_;
                        f_351_ += f_396_;
                        f_350_ += f_386_;
                        f_354_ += f_397_;
                        f_353_ += f_387_;
                        i_360_ += f_398_;
                        i_359_ += f_388_;
                        f_364_ += f_399_;
                        f_363_ += f_389_;
                        f_367_ += f_400_;
                        f_366_ += f_390_;
                        f_370_ += f_401_;
                        f_369_ += f_391_;
                        f_373_ += f_402_;
                        f_372_ += f_392_;
                        f_342_ += (float) anInt1678;
                    }
                    while (--f_341_ >= 0.0F) {
                        method1016(anIntArray1673, anIntArray1698, (int) f_342_, (int) f_343_, (int) f_344_, f_346_, f_347_, f_349_, f_350_, f_352_, f_353_, (float) i_358_, (float) i_359_, f_362_, f_363_, f_365_, f_366_, f_368_, f_369_, f_371_, f_372_);
                        f_343_ += f_374_;
                        f_344_ += f_384_;
                        f_346_ += f_375_;
                        f_347_ += f_385_;
                        f_349_ += f_376_;
                        f_350_ += f_386_;
                        f_352_ += f_377_;
                        f_353_ += f_387_;
                        i_358_ += f_378_;
                        i_359_ += f_388_;
                        f_362_ += f_379_;
                        f_363_ += f_389_;
                        f_365_ += f_380_;
                        f_366_ += f_390_;
                        f_368_ += f_381_;
                        f_369_ += f_391_;
                        f_371_ += f_382_;
                        f_372_ += f_392_;
                        f_342_ += (float) anInt1678;
                    }
                }
            } else {
                f_343_ = f_345_;
                f_346_ = f_348_;
                f_349_ = f_351_;
                f_352_ = f_354_;
                i_358_ = i_360_;
                f_362_ = f_364_;
                f_365_ = f_367_;
                f_368_ = f_370_;
                f_371_ = f_373_;
                if (f_342_ < 0.0F) {
                    f_345_ -= f_394_ * f_342_;
                    f_343_ -= f_384_ * f_342_;
                    f_348_ -= f_395_ * f_342_;
                    f_346_ -= f_385_ * f_342_;
                    f_351_ -= f_396_ * f_342_;
                    f_349_ -= f_386_ * f_342_;
                    f_354_ -= f_397_ * f_342_;
                    f_352_ -= f_387_ * f_342_;
                    i_360_ -= f_398_ * 3.0F;
                    i_358_ -= f_388_ * f_342_;
                    f_364_ -= f_399_ * f_342_;
                    f_362_ -= f_389_ * f_342_;
                    f_367_ -= f_400_ * f_342_;
                    f_365_ -= f_390_ * f_342_;
                    f_370_ -= f_401_ * f_342_;
                    f_368_ -= f_391_ * f_342_;
                    f_373_ -= f_402_ * f_342_;
                    f_371_ -= f_392_ * f_342_;
                    f_342_ = 0.0F;
                }
                if (f_341_ < 0.0F) {
                    f_344_ -= f_374_ * f_341_;
                    f_347_ -= f_375_ * f_341_;
                    f_350_ -= f_376_ * f_341_;
                    f_353_ -= f_377_ * f_341_;
                    i_359_ -= f_378_ * f_341_;
                    f_363_ -= f_379_ * f_341_;
                    f_366_ -= f_380_ * f_341_;
                    f_369_ -= f_381_ * f_341_;
                    f_372_ -= f_382_ * f_341_;
                    f_341_ = 0.0F;
                }
                if (f_384_ < f_394_) {
                    f -= f_341_;
                    f_341_ -= f_342_;
                    f_342_ = (float) (this.lineOffsets[(int) f_342_]);
                    while (--f_341_ >= 0.0F) {
                        method1016(anIntArray1673, anIntArray1698, (int) f_342_, (int) f_343_, (int) f_345_, f_346_, f_348_, f_349_, f_351_, f_352_, f_354_, (float) i_358_, (float) i_360_, f_362_, f_364_, f_365_, f_367_, f_368_, f_370_, f_371_, f_373_);
                        f_343_ += f_384_;
                        f_345_ += f_394_;
                        f_346_ += f_385_;
                        f_348_ += f_395_;
                        f_349_ += f_386_;
                        f_351_ += f_396_;
                        f_352_ += f_387_;
                        f_354_ += f_397_;
                        i_358_ += f_388_;
                        i_360_ += f_398_;
                        f_362_ += f_389_;
                        f_364_ += f_399_;
                        f_365_ += f_390_;
                        f_367_ += f_400_;
                        f_368_ += f_391_;
                        f_370_ += f_401_;
                        f_371_ += f_392_;
                        f_373_ += f_402_;
                        f_342_ += (float) anInt1678;
                    }
                    while (--f >= 0.0F) {
                        method1016(anIntArray1673, anIntArray1698, (int) f_342_, (int) f_344_, (int) f_345_, f_347_, f_348_, f_350_, f_351_, f_353_, f_354_, (float) i_359_, (float) i_360_, f_363_, f_364_, f_366_, f_367_, f_369_, f_370_, f_372_, f_373_);
                        f_344_ += f_374_;
                        f_345_ += f_394_;
                        f_347_ += f_375_;
                        f_348_ += f_395_;
                        f_350_ += f_376_;
                        f_351_ += f_396_;
                        f_353_ += f_377_;
                        f_354_ += f_397_;
                        i_359_ += f_378_;
                        i_360_ += f_398_;
                        f_363_ += f_379_;
                        f_364_ += f_399_;
                        f_366_ += f_380_;
                        f_367_ += f_400_;
                        f_369_ += f_381_;
                        f_370_ += f_401_;
                        f_372_ += f_382_;
                        f_373_ += f_402_;
                        f_342_ += (float) anInt1678;
                    }
                } else {
                    f -= f_341_;
                    f_341_ -= f_342_;
                    f_342_ = (float) (this.lineOffsets[(int) f_342_]);
                    while (--f_341_ >= 0.0F) {
                        method1016(anIntArray1673, anIntArray1698, (int) f_342_, (int) f_345_, (int) f_343_, f_348_, f_346_, f_351_, f_349_, f_354_, f_352_, (float) i_360_, (float) i_358_, f_364_, f_362_, f_367_, f_365_, f_370_, f_368_, f_373_, f_371_);
                        f_345_ += f_394_;
                        f_343_ += f_384_;
                        f_348_ += f_395_;
                        f_346_ += f_385_;
                        f_351_ += f_396_;
                        f_349_ += f_386_;
                        f_354_ += f_397_;
                        f_352_ += f_387_;
                        i_360_ += f_398_;
                        i_358_ += f_388_;
                        f_364_ += f_399_;
                        f_362_ += f_389_;
                        f_367_ += f_400_;
                        f_365_ += f_390_;
                        f_370_ += f_401_;
                        f_368_ += f_391_;
                        f_373_ += f_402_;
                        f_371_ += f_392_;
                        f_342_ += (float) anInt1678;
                    }
                    while (--f >= 0.0F) {
                        method1016(anIntArray1673, anIntArray1698, (int) f_342_, (int) f_345_, (int) f_344_, f_348_, f_347_, f_351_, f_350_, f_354_, f_353_, (float) i_360_, (float) i_359_, f_364_, f_363_, f_367_, f_366_, f_370_, f_369_, f_373_, f_372_);
                        f_345_ += f_394_;
                        f_344_ += f_374_;
                        f_348_ += f_395_;
                        f_347_ += f_375_;
                        f_351_ += f_396_;
                        f_350_ += f_376_;
                        f_354_ += f_397_;
                        f_353_ += f_377_;
                        i_360_ += f_398_;
                        i_359_ += f_378_;
                        f_364_ += f_399_;
                        f_363_ += f_379_;
                        f_367_ += f_400_;
                        f_366_ += f_380_;
                        f_370_ += f_401_;
                        f_369_ += f_391_;
                        f_373_ += f_402_;
                        f_372_ += f_392_;
                        f_342_ += (float) anInt1678;
                    }
                }
            }
        }
    }

    private final void method1026(int[] is, float[] fs, int i, int i_450_, int i_451_, int i_452_, int i_453_, float f, float f_454_) {
        if (this.clamp) {
            if (i_453_ > this.width) i_453_ = this.width;
            if (i_452_ < 0) i_452_ = 0;
        }
        if (i_452_ < i_453_) {
            i += i_452_ - 1;
            i_451_ = i_453_ - i_452_ >> 2;
            f += f_454_ * (float) i_452_;
            if (aJavaThreadResource_1670.aBoolean2202) {
                if (this.anInt1674 == 0) {
                    while (--i_451_ >= 0) {
                        if (f < fs[++i]) {
                            is[i] = i_450_;
                            fs[i] = f;
                        }
                        f += f_454_;
                        if (f < fs[++i]) {
                            is[i] = i_450_;
                            fs[i] = f;
                        }
                        f += f_454_;
                        if (f < fs[++i]) {
                            is[i] = i_450_;
                            fs[i] = f;
                        }
                        f += f_454_;
                        if (f < fs[++i]) {
                            is[i] = i_450_;
                            fs[i] = f;
                        }
                        f += f_454_;
                    }
                    i_451_ = i_453_ - i_452_ & 0x3;
                    while (--i_451_ >= 0) {
                        if (f < fs[++i]) {
                            is[i] = i_450_;
                            fs[i] = f;
                        }
                        f += f_454_;
                    }
                } else if (this.anInt1674 == 254) {
                    if (i_452_ != 0 && i_453_ <= this.width - 1) {
                        while (--i_451_ >= 0) {
                            if (f < fs[++i]) is[i - 1] = is[i];
                            f += f_454_;
                            if (f < fs[++i]) is[i - 1] = is[i];
                            f += f_454_;
                            if (f < fs[++i]) is[i - 1] = is[i];
                            f += f_454_;
                            if (f < fs[++i]) is[i - 1] = is[i];
                            f += f_454_;
                        }
                        i_451_ = i_453_ - i_452_ & 0x3;
                        while (--i_451_ >= 0) {
                            if (f < fs[++i]) is[i - 1] = is[i];
                            f += f_454_;
                        }
                    }
                } else {
                    int i_455_ = this.anInt1674;
                    int i_456_ = 256 - this.anInt1674;
                    i_450_ = (((i_450_ & 0xff00ff) * i_456_ >> 8 & 0xff00ff) + ((i_450_ & 0xff00) * i_456_ >> 8 & 0xff00));
                    while (--i_451_ >= 0) {
                        if (f < fs[++i]) {
                            int i_457_ = is[i];
                            is[i] = (i_450_ + ((i_457_ & 0xff00ff) * i_455_ >> 8 & 0xff00ff) + ((i_457_ & 0xff00) * i_455_ >> 8 & 0xff00));
                            fs[i] = f;
                        }
                        f += f_454_;
                        if (f < fs[++i]) {
                            int i_458_ = is[i];
                            is[i] = (i_450_ + ((i_458_ & 0xff00ff) * i_455_ >> 8 & 0xff00ff) + ((i_458_ & 0xff00) * i_455_ >> 8 & 0xff00));
                            fs[i] = f;
                        }
                        f += f_454_;
                        if (f < fs[++i]) {
                            int i_459_ = is[i];
                            is[i] = (i_450_ + ((i_459_ & 0xff00ff) * i_455_ >> 8 & 0xff00ff) + ((i_459_ & 0xff00) * i_455_ >> 8 & 0xff00));
                            fs[i] = f;
                        }
                        f += f_454_;
                        if (f < fs[++i]) {
                            int i_460_ = is[i];
                            is[i] = (i_450_ + ((i_460_ & 0xff00ff) * i_455_ >> 8 & 0xff00ff) + ((i_460_ & 0xff00) * i_455_ >> 8 & 0xff00));
                            fs[i] = f;
                        }
                        f += f_454_;
                    }
                    i_451_ = i_453_ - i_452_ & 0x3;
                    while (--i_451_ >= 0) {
                        if (f < fs[++i]) {
                            int i_461_ = is[i];
                            is[i] = (i_450_ + ((i_461_ & 0xff00ff) * i_455_ >> 8 & 0xff00ff) + ((i_461_ & 0xff00) * i_455_ >> 8 & 0xff00));
                            fs[i] = f;
                        }
                        f += f_454_;
                    }
                }
            } else if (this.anInt1674 == 0) {
                while (--i_451_ >= 0) {
                    if (f < fs[++i]) is[i] = i_450_;
                    f += f_454_;
                    if (f < fs[++i]) is[i] = i_450_;
                    f += f_454_;
                    if (f < fs[++i]) is[i] = i_450_;
                    f += f_454_;
                    if (f < fs[++i]) is[i] = i_450_;
                    f += f_454_;
                }
                i_451_ = i_453_ - i_452_ & 0x3;
                while (--i_451_ >= 0) {
                    if (f < fs[++i]) is[i] = i_450_;
                    f += f_454_;
                }
            } else if (this.anInt1674 == 254) {
                if (i_452_ != 0 && i_453_ <= this.width - 1) {
                    while (--i_451_ >= 0) {
                        if (f < fs[++i]) is[i - 1] = is[i];
                        f += f_454_;
                        if (f < fs[++i]) is[i - 1] = is[i];
                        f += f_454_;
                        if (f < fs[++i]) is[i - 1] = is[i];
                        f += f_454_;
                        if (f < fs[++i]) is[i - 1] = is[i];
                        f += f_454_;
                    }
                    i_451_ = i_453_ - i_452_ & 0x3;
                    while (--i_451_ >= 0) {
                        if (f < fs[++i]) is[i - 1] = is[i];
                        f += f_454_;
                    }
                }
            } else {
                int i_462_ = this.anInt1674;
                int i_463_ = 256 - this.anInt1674;
                i_450_ = (((i_450_ & 0xff00ff) * i_463_ >> 8 & 0xff00ff) + ((i_450_ & 0xff00) * i_463_ >> 8 & 0xff00));
                while (--i_451_ >= 0) {
                    if (f < fs[++i]) {
                        int i_464_ = is[i];
                        is[i] = (i_450_ + ((i_464_ & 0xff00ff) * i_462_ >> 8 & 0xff00ff) + ((i_464_ & 0xff00) * i_462_ >> 8 & 0xff00));
                    }
                    f += f_454_;
                    if (f < fs[++i]) {
                        int i_465_ = is[i];
                        is[i] = (i_450_ + ((i_465_ & 0xff00ff) * i_462_ >> 8 & 0xff00ff) + ((i_465_ & 0xff00) * i_462_ >> 8 & 0xff00));
                    }
                    f += f_454_;
                    if (f < fs[++i]) {
                        int i_466_ = is[i];
                        is[i] = (i_450_ + ((i_466_ & 0xff00ff) * i_462_ >> 8 & 0xff00ff) + ((i_466_ & 0xff00) * i_462_ >> 8 & 0xff00));
                    }
                    f += f_454_;
                    if (f < fs[++i]) {
                        int i_467_ = is[i];
                        is[i] = (i_450_ + ((i_467_ & 0xff00ff) * i_462_ >> 8 & 0xff00ff) + ((i_467_ & 0xff00) * i_462_ >> 8 & 0xff00));
                    }
                    f += f_454_;
                }
                i_451_ = i_453_ - i_452_ & 0x3;
                while (--i_451_ >= 0) {
                    if (f < fs[++i]) {
                        int i_468_ = is[i];
                        is[i] = (i_450_ + ((i_468_ & 0xff00ff) * i_462_ >> 8 & 0xff00ff) + ((i_468_ & 0xff00) * i_462_ >> 8 & 0xff00));
                    }
                    f += f_454_;
                }
            }
        }
    }

    final void method1027(float f, float f_469_, float f_470_, float f_471_, float f_472_, float f_473_, float f_474_, float f_475_, float f_476_, int i, int i_477_, int i_478_) {
        if (aBoolean1675) {
            aHa_Sub1_1666.line((int) f, (int) f_471_, (int) f_472_, -8003, ~0xffffff | i, (int) f_469_);
            aHa_Sub1_1666.line((int) f_469_, (int) f_472_, (int) f_473_, -8003, ~0xffffff | i, (int) f_470_);
            aHa_Sub1_1666.line((int) f_470_, (int) f_473_, (int) f_471_, -8003, ~0xffffff | i, (int) f);
        } else {
            float f_479_ = f_472_ - f_471_;
            float f_480_ = f_469_ - f;
            float f_481_ = f_473_ - f_471_;
            float f_482_ = f_470_ - f;
            float f_483_ = f_475_ - f_474_;
            float f_484_ = f_476_ - f_474_;
            float f_485_ = (float) ((i_477_ & 0xff0000) - (i & 0xff0000));
            float f_486_ = (float) ((i_478_ & 0xff0000) - (i & 0xff0000));
            float f_487_ = (float) ((i_477_ & 0xff00) - (i & 0xff00));
            float f_488_ = (float) ((i_478_ & 0xff00) - (i & 0xff00));
            float f_489_ = (float) ((i_477_ & 0xff) - (i & 0xff));
            float f_490_ = (float) ((i_478_ & 0xff) - (i & 0xff));
            float f_491_;
            if (f_470_ != f_469_) f_491_ = (f_473_ - f_472_) / (f_470_ - f_469_);
            else f_491_ = 0.0F;
            float f_492_;
            if (f_469_ != f) f_492_ = f_479_ / f_480_;
            else f_492_ = 0.0F;
            float f_493_;
            if (f_470_ != f) f_493_ = f_481_ / f_482_;
            else f_493_ = 0.0F;
            float f_494_ = f_479_ * f_482_ - f_481_ * f_480_;
            if (f_494_ != 0.0F) {
                float f_495_ = (f_483_ * f_482_ - f_484_ * f_480_) / f_494_;
                float f_496_ = (f_484_ * f_479_ - f_483_ * f_481_) / f_494_;
                float f_497_ = (f_485_ * f_482_ - f_486_ * f_480_) / f_494_;
                float f_498_ = (f_486_ * f_479_ - f_485_ * f_481_) / f_494_;
                float f_499_ = (f_487_ * f_482_ - f_488_ * f_480_) / f_494_;
                float f_500_ = (f_488_ * f_479_ - f_487_ * f_481_) / f_494_;
                float f_501_ = (f_489_ * f_482_ - f_490_ * f_480_) / f_494_;
                float f_502_ = (f_490_ * f_479_ - f_489_ * f_481_) / f_494_;
                if (f <= f_469_ && f <= f_470_) {
                    if (!(f >= (float) this.height)) {
                        if (f_469_ > (float) this.height) f_469_ = (float) this.height;
                        if (f_470_ > (float) this.height) f_470_ = (float) this.height;
                        f_474_ = f_474_ - f_495_ * f_471_ + f_495_;
                        float f_503_ = ((float) (i & 0xff0000) - f_497_ * f_471_ + f_497_);
                        float f_504_ = (float) (i & 0xff00) - f_499_ * f_471_ + f_499_;
                        float f_505_ = (float) (i & 0xff) - f_501_ * f_471_ + f_501_;
                        if (f_469_ < f_470_) {
                            f_473_ = f_471_;
                            if (f < 0.0F) {
                                f_473_ -= f_493_ * f;
                                f_471_ -= f_492_ * f;
                                f_474_ -= f_496_ * f;
                                f_503_ -= f_498_ * f;
                                f_504_ -= f_500_ * f;
                                f_505_ -= f_502_ * f;
                                f = 0.0F;
                            }
                            if (f_469_ < 0.0F) {
                                f_472_ -= f_491_ * f_469_;
                                f_469_ = 0.0F;
                            }
                            if (f != f_469_ && f_493_ < f_492_ || f == f_469_ && f_493_ > f_491_) {
                                f_470_ -= f_469_;
                                f_469_ -= f;
                                f = (float) (this.lineOffsets[(int) f]);
                                while (--f_469_ >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_473_, (int) f_471_, f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_);
                                    f_473_ += f_493_;
                                    f_471_ += f_492_;
                                    f_474_ += f_496_;
                                    f_503_ += f_498_;
                                    f_504_ += f_500_;
                                    f_505_ += f_502_;
                                    f += (float) anInt1678;
                                }
                                while (--f_470_ >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_473_, (int) f_472_, f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_);
                                    f_473_ += f_493_;
                                    f_472_ += f_491_;
                                    f_474_ += f_496_;
                                    f_503_ += f_498_;
                                    f_504_ += f_500_;
                                    f_505_ += f_502_;
                                    f += (float) anInt1678;
                                }
                            } else {
                                f_470_ -= f_469_;
                                f_469_ -= f;
                                f = (float) (this.lineOffsets[(int) f]);
                                while (--f_469_ >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_471_, (int) f_473_, f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_);
                                    f_473_ += f_493_;
                                    f_471_ += f_492_;
                                    f_474_ += f_496_;
                                    f_503_ += f_498_;
                                    f_504_ += f_500_;
                                    f_505_ += f_502_;
                                    f += (float) anInt1678;
                                }
                                while (--f_470_ >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_472_, (int) f_473_, f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_);
                                    f_473_ += f_493_;
                                    f_472_ += f_491_;
                                    f_474_ += f_496_;
                                    f_503_ += f_498_;
                                    f_504_ += f_500_;
                                    f_505_ += f_502_;
                                    f += (float) anInt1678;
                                }
                            }
                        } else {
                            f_472_ = f_471_;
                            if (f < 0.0F) {
                                f_472_ -= f_493_ * f;
                                f_471_ -= f_492_ * f;
                                f_474_ -= f_496_ * f;
                                f_503_ -= f_498_ * f;
                                f_504_ -= f_500_ * f;
                                f_505_ -= f_502_ * f;
                                f = 0.0F;
                            }
                            if (f_470_ < 0.0F) {
                                f_473_ -= f_491_ * f_470_;
                                f_470_ = 0.0F;
                            }
                            if (f != f_470_ && f_493_ < f_492_ || f == f_470_ && f_491_ > f_492_) {
                                f_469_ -= f_470_;
                                f_470_ -= f;
                                f = (float) (this.lineOffsets[(int) f]);
                                while (--f_470_ >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_472_, (int) f_471_, f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_);
                                    f_472_ += f_493_;
                                    f_471_ += f_492_;
                                    f_474_ += f_496_;
                                    f_503_ += f_498_;
                                    f_504_ += f_500_;
                                    f_505_ += f_502_;
                                    f += (float) anInt1678;
                                }
                                while (--f_469_ >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_473_, (int) f_471_, f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_);
                                    f_473_ += f_491_;
                                    f_471_ += f_492_;
                                    f_474_ += f_496_;
                                    f_503_ += f_498_;
                                    f_504_ += f_500_;
                                    f_505_ += f_502_;
                                    f += (float) anInt1678;
                                }
                            } else {
                                f_469_ -= f_470_;
                                f_470_ -= f;
                                f = (float) (this.lineOffsets[(int) f]);
                                while (--f_470_ >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_471_, (int) f_472_, f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_);
                                    f_472_ += f_493_;
                                    f_471_ += f_492_;
                                    f_474_ += f_496_;
                                    f_503_ += f_498_;
                                    f_504_ += f_500_;
                                    f_505_ += f_502_;
                                    f += (float) anInt1678;
                                }
                                while (--f_469_ >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f, 0, 0, (int) f_471_, (int) f_473_, f_474_, f_495_, f_503_, f_497_, f_504_, f_499_, f_505_, f_501_);
                                    f_473_ += f_491_;
                                    f_471_ += f_492_;
                                    f_474_ += f_496_;
                                    f_503_ += f_498_;
                                    f_504_ += f_500_;
                                    f_505_ += f_502_;
                                    f += (float) anInt1678;
                                }
                            }
                        }
                    }
                } else if (f_469_ <= f_470_) {
                    if (!(f_469_ >= (float) this.height)) {
                        if (f_470_ > (float) this.height) f_470_ = (float) this.height;
                        if (f > (float) this.height) f = (float) this.height;
                        f_475_ = f_475_ - f_495_ * f_472_ + f_495_;
                        float f_506_ = ((float) (i_477_ & 0xff0000) - f_497_ * f_472_ + f_497_);
                        float f_507_ = ((float) (i_477_ & 0xff00) - f_499_ * f_472_ + f_499_);
                        float f_508_ = ((float) (i_477_ & 0xff) - f_501_ * f_472_ + f_501_);
                        if (f_470_ < f) {
                            f_471_ = f_472_;
                            if (f_469_ < 0.0F) {
                                f_471_ -= f_492_ * f_469_;
                                f_472_ -= f_491_ * f_469_;
                                f_475_ -= f_496_ * f_469_;
                                f_506_ -= f_498_ * f_469_;
                                f_507_ -= f_500_ * f_469_;
                                f_508_ -= f_502_ * f_469_;
                                f_469_ = 0.0F;
                            }
                            if (f_470_ < 0.0F) {
                                f_473_ -= f_493_ * f_470_;
                                f_470_ = 0.0F;
                            }
                            if (f_469_ != f_470_ && f_492_ < f_491_ || f_469_ == f_470_ && f_492_ > f_493_) {
                                f -= f_470_;
                                f_470_ -= f_469_;
                                f_469_ = (float) (this.lineOffsets[(int) f_469_]);
                                while (--f_470_ >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f_469_, 0, 0, (int) f_471_, (int) f_472_, f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_);
                                    f_471_ += f_492_;
                                    f_472_ += f_491_;
                                    f_475_ += f_496_;
                                    f_506_ += f_498_;
                                    f_507_ += f_500_;
                                    f_508_ += f_502_;
                                    f_469_ += (float) anInt1678;
                                }
                                while (--f >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f_469_, 0, 0, (int) f_471_, (int) f_473_, f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_);
                                    f_471_ += f_492_;
                                    f_473_ += f_493_;
                                    f_475_ += f_496_;
                                    f_506_ += f_498_;
                                    f_507_ += f_500_;
                                    f_508_ += f_502_;
                                    f_469_ += (float) anInt1678;
                                }
                            } else {
                                f -= f_470_;
                                f_470_ -= f_469_;
                                f_469_ = (float) (this.lineOffsets[(int) f_469_]);
                                while (--f_470_ >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f_469_, 0, 0, (int) f_472_, (int) f_471_, f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_);
                                    f_471_ += f_492_;
                                    f_472_ += f_491_;
                                    f_475_ += f_496_;
                                    f_506_ += f_498_;
                                    f_507_ += f_500_;
                                    f_508_ += f_502_;
                                    f_469_ += (float) anInt1678;
                                }
                                while (--f >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f_469_, 0, 0, (int) f_473_, (int) f_471_, f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_);
                                    f_471_ += f_492_;
                                    f_473_ += f_493_;
                                    f_475_ += f_496_;
                                    f_506_ += f_498_;
                                    f_507_ += f_500_;
                                    f_508_ += f_502_;
                                    f_469_ += (float) anInt1678;
                                }
                            }
                        } else {
                            f_473_ = f_472_;
                            if (f_469_ < 0.0F) {
                                f_473_ -= f_492_ * f_469_;
                                f_472_ -= f_491_ * f_469_;
                                f_475_ -= f_496_ * f_469_;
                                f_506_ -= f_498_ * f_469_;
                                f_507_ -= f_500_ * f_469_;
                                f_508_ -= f_502_ * f_469_;
                                f_469_ = 0.0F;
                            }
                            if (f < 0.0F) {
                                f_471_ -= f_493_ * f;
                                f = 0.0F;
                            }
                            if (f_492_ < f_491_) {
                                f_470_ -= f;
                                f -= f_469_;
                                f_469_ = (float) (this.lineOffsets[(int) f_469_]);
                                while (--f >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f_469_, 0, 0, (int) f_473_, (int) f_472_, f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_);
                                    f_473_ += f_492_;
                                    f_472_ += f_491_;
                                    f_475_ += f_496_;
                                    f_506_ += f_498_;
                                    f_507_ += f_500_;
                                    f_508_ += f_502_;
                                    f_469_ += (float) anInt1678;
                                }
                                while (--f_470_ >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f_469_, 0, 0, (int) f_471_, (int) f_472_, f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_);
                                    f_471_ += f_493_;
                                    f_472_ += f_491_;
                                    f_475_ += f_496_;
                                    f_506_ += f_498_;
                                    f_507_ += f_500_;
                                    f_508_ += f_502_;
                                    f_469_ += (float) anInt1678;
                                }
                            } else {
                                f_470_ -= f;
                                f -= f_469_;
                                f_469_ = (float) (this.lineOffsets[(int) f_469_]);
                                while (--f >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f_469_, 0, 0, (int) f_472_, (int) f_473_, f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_);
                                    f_473_ += f_492_;
                                    f_472_ += f_491_;
                                    f_475_ += f_496_;
                                    f_506_ += f_498_;
                                    f_507_ += f_500_;
                                    f_508_ += f_502_;
                                    f_469_ += (float) anInt1678;
                                }
                                while (--f_470_ >= 0.0F) {
                                    method1021(anIntArray1673, aFloatArray1677, (int) f_469_, 0, 0, (int) f_472_, (int) f_471_, f_475_, f_495_, f_506_, f_497_, f_507_, f_499_, f_508_, f_501_);
                                    f_471_ += f_493_;
                                    f_472_ += f_491_;
                                    f_475_ += f_496_;
                                    f_506_ += f_498_;
                                    f_507_ += f_500_;
                                    f_508_ += f_502_;
                                    f_469_ += (float) anInt1678;
                                }
                            }
                        }
                    }
                } else if (!(f_470_ >= (float) this.height)) {
                    if (f > (float) this.height) f = (float) this.height;
                    if (f_469_ > (float) this.height) f_469_ = (float) this.height;
                    f_476_ = f_476_ - f_495_ * f_473_ + f_495_;
                    float f_509_ = ((float) (i_478_ & 0xff0000) - f_497_ * f_473_ + f_497_);
                    float f_510_ = (float) (i_478_ & 0xff00) - f_499_ * f_473_ + f_499_;
                    float f_511_ = (float) (i_478_ & 0xff) - f_501_ * f_473_ + f_501_;
                    if (f < f_469_) {
                        f_472_ = f_473_;
                        if (f_470_ < 0.0F) {
                            f_472_ -= f_491_ * f_470_;
                            f_473_ -= f_493_ * f_470_;
                            f_476_ -= f_496_ * f_470_;
                            f_509_ -= f_498_ * f_470_;
                            f_510_ -= f_500_ * f_470_;
                            f_511_ -= f_502_ * f_470_;
                            f_470_ = 0.0F;
                        }
                        if (f < 0.0F) {
                            f_471_ -= f_492_ * f;
                            f = 0.0F;
                        }
                        if (f_491_ < f_493_) {
                            f_469_ -= f;
                            f -= f_470_;
                            f_470_ = (float) (this.lineOffsets[(int) f_470_]);
                            while (--f >= 0.0F) {
                                method1021(anIntArray1673, aFloatArray1677, (int) f_470_, 0, 0, (int) f_472_, (int) f_473_, f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_);
                                f_472_ += f_491_;
                                f_473_ += f_493_;
                                f_476_ += f_496_;
                                f_509_ += f_498_;
                                f_510_ += f_500_;
                                f_511_ += f_502_;
                                f_470_ += (float) anInt1678;
                            }
                            while (--f_469_ >= 0.0F) {
                                method1021(anIntArray1673, aFloatArray1677, (int) f_470_, 0, 0, (int) f_472_, (int) f_471_, f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_);
                                f_472_ += f_491_;
                                f_471_ += f_492_;
                                f_476_ += f_496_;
                                f_509_ += f_498_;
                                f_510_ += f_500_;
                                f_511_ += f_502_;
                                f_470_ += (float) anInt1678;
                            }
                        } else {
                            f_469_ -= f;
                            f -= f_470_;
                            f_470_ = (float) (this.lineOffsets[(int) f_470_]);
                            while (--f >= 0.0F) {
                                method1021(anIntArray1673, aFloatArray1677, (int) f_470_, 0, 0, (int) f_473_, (int) f_472_, f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_);
                                f_472_ += f_491_;
                                f_473_ += f_493_;
                                f_476_ += f_496_;
                                f_509_ += f_498_;
                                f_510_ += f_500_;
                                f_511_ += f_502_;
                                f_470_ += (float) anInt1678;
                            }
                            while (--f_469_ >= 0.0F) {
                                method1021(anIntArray1673, aFloatArray1677, (int) f_470_, 0, 0, (int) f_471_, (int) f_472_, f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_);
                                f_472_ += f_491_;
                                f_471_ += f_492_;
                                f_476_ += f_496_;
                                f_509_ += f_498_;
                                f_510_ += f_500_;
                                f_511_ += f_502_;
                                f_470_ += (float) anInt1678;
                            }
                        }
                    } else {
                        f_471_ = f_473_;
                        if (f_470_ < 0.0F) {
                            f_471_ -= f_491_ * f_470_;
                            f_473_ -= f_493_ * f_470_;
                            f_476_ -= f_496_ * f_470_;
                            f_509_ -= f_498_ * f_470_;
                            f_510_ -= f_500_ * f_470_;
                            f_511_ -= f_502_ * f_470_;
                            f_470_ = 0.0F;
                        }
                        if (f_469_ < 0.0F) {
                            f_472_ -= f_492_ * f_469_;
                            f_469_ = 0.0F;
                        }
                        if (f_491_ < f_493_) {
                            f -= f_469_;
                            f_469_ -= f_470_;
                            f_470_ = (float) (this.lineOffsets[(int) f_470_]);
                            while (--f_469_ >= 0.0F) {
                                method1021(anIntArray1673, aFloatArray1677, (int) f_470_, 0, 0, (int) f_471_, (int) f_473_, f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_);
                                f_471_ += f_491_;
                                f_473_ += f_493_;
                                f_476_ += f_496_;
                                f_509_ += f_498_;
                                f_510_ += f_500_;
                                f_511_ += f_502_;
                                f_470_ += (float) anInt1678;
                            }
                            while (--f >= 0.0F) {
                                method1021(anIntArray1673, aFloatArray1677, (int) f_470_, 0, 0, (int) f_472_, (int) f_473_, f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_);
                                f_472_ += f_492_;
                                f_473_ += f_493_;
                                f_476_ += f_496_;
                                f_509_ += f_498_;
                                f_510_ += f_500_;
                                f_511_ += f_502_;
                                f_470_ += (float) anInt1678;
                            }
                        } else {
                            f -= f_469_;
                            f_469_ -= f_470_;
                            f_470_ = (float) (this.lineOffsets[(int) f_470_]);
                            while (--f_469_ >= 0.0F) {
                                method1021(anIntArray1673, aFloatArray1677, (int) f_470_, 0, 0, (int) f_473_, (int) f_471_, f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_);
                                f_471_ += f_491_;
                                f_473_ += f_493_;
                                f_476_ += f_496_;
                                f_509_ += f_498_;
                                f_510_ += f_500_;
                                f_511_ += f_502_;
                                f_470_ += (float) anInt1678;
                            }
                            while (--f >= 0.0F) {
                                method1021(anIntArray1673, aFloatArray1677, (int) f_470_, 0, 0, (int) f_473_, (int) f_472_, f_476_, f_495_, f_509_, f_497_, f_510_, f_499_, f_511_, f_501_);
                                f_472_ += f_492_;
                                f_473_ += f_493_;
                                f_476_ += f_496_;
                                f_509_ += f_498_;
                                f_510_ += f_500_;
                                f_511_ += f_502_;
                                f_470_ += (float) anInt1678;
                            }
                        }
                    }
                }
            }
        }
    }

    final int method1028() {
        return this.lineOffsets[0] % anInt1678;
    }

    Rasterizer(JavaToolkit var_ha_Sub1, JavaThreadResource javaThreadResource) {
        this.aBoolean1669 = true;
        this.clamp = false;
        aBoolean1680 = false;
        aFloat1682 = 0.0F;
        anIntArray1685 = null;
        anInt1687 = -1;
        aFloat1684 = 0.0F;
        anInt1693 = 0;
        anInt1691 = 0;
        anIntArray1692 = null;
        anInt1689 = -1;
        anInt1683 = 0;
        aFloat1681 = 0.0F;
        aBoolean1694 = true;
        anInt1688 = 0;
        anInt1695 = 0;
        anInt1686 = 0;
        anInt1690 = 0;
        anInt1697 = -1;
        anIntArray1698 = null;
        aHa_Sub1_1666 = var_ha_Sub1;
        aJavaThreadResource_1670 = javaThreadResource;
        anInt1678 = aHa_Sub1_1666.anInt7477;
        anIntArray1673 = aHa_Sub1_1666.anIntArray7483;
        aFloatArray1677 = aHa_Sub1_1666.aFloatArray7511;
    }
}
