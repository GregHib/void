package world.gregs.voidps.tools.inv.item;/* Class105_Sub3_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class JavaRgbSprite extends Sprite_Sub3 {
    int[] anIntArray9933;

    final void method964(int i, int i_883_, int i_884_, int i_885_, int i_886_) {
        if (this.aHa_Sub1_8460.method3716()) throw new IllegalStateException();
        int i_887_ = this.aHa_Sub1_8460.anInt7477;
        i += this.anInt8461;
        i_883_ += this.anInt8464;
        int i_888_ = i_883_ * i_887_ + i;
        int i_889_ = 0;
        int i_890_ = this.anInt8470;
        int i_891_ = this.anInt8471;
        int i_892_ = i_887_ - i_891_;
        int i_893_ = 0;
        if (i_883_ < this.aHa_Sub1_8460.anInt7476) {
            int i_894_ = (this.aHa_Sub1_8460.anInt7476 - i_883_);
            i_890_ -= i_894_;
            i_883_ = this.aHa_Sub1_8460.anInt7476;
            i_889_ += i_894_ * i_891_;
            i_888_ += i_894_ * i_887_;
        }
        if (i_883_ + i_890_ > this.aHa_Sub1_8460.anInt7503) i_890_ -= i_883_ + i_890_ - this.aHa_Sub1_8460.anInt7503;
        if (i < this.aHa_Sub1_8460.anInt7496) {
            int i_895_ = (this.aHa_Sub1_8460.anInt7496 - i);
            i_891_ -= i_895_;
            i = this.aHa_Sub1_8460.anInt7496;
            i_889_ += i_895_;
            i_888_ += i_895_;
            i_893_ += i_895_;
            i_892_ += i_895_;
        }
        if (i + i_891_ > this.aHa_Sub1_8460.anInt7507) {
            int i_896_ = (i + i_891_ - (this.aHa_Sub1_8460.anInt7507));
            i_891_ -= i_896_;
            i_893_ += i_896_;
            i_892_ += i_896_;
        }
        if (i_891_ > 0 && i_890_ > 0) {
            int[] is = (this.aHa_Sub1_8460.anIntArray7483);
            if (i_886_ == 0) {
                if (i_884_ == 1) {
                    for (int i_897_ = -i_890_; i_897_ < 0; i_897_++) {
                        int i_898_ = i_888_ + i_891_ - 3;
                        while (i_888_ < i_898_) {
                            is[i_888_++] = (this.anIntArray9933[i_889_++]);
                            is[i_888_++] = (this.anIntArray9933[i_889_++]);
                            is[i_888_++] = (this.anIntArray9933[i_889_++]);
                            is[i_888_++] = (this.anIntArray9933[i_889_++]);
                        }
                        i_898_ += 3;
                        while (i_888_ < i_898_) is[i_888_++] = (this.anIntArray9933[i_889_++]);
                        i_888_ += i_892_;
                        i_889_ += i_893_;
                    }
                    return;
                }
                if (i_884_ == 0) {
                    int i_899_ = (i_885_ & 0xff0000) >> 16;
                    int i_900_ = (i_885_ & 0xff00) >> 8;
                    int i_901_ = i_885_ & 0xff;
                    for (int i_902_ = -i_890_; i_902_ < 0; i_902_++) {
                        for (int i_903_ = -i_891_; i_903_ < 0; i_903_++) {
                            int i_904_ = (this.anIntArray9933[i_889_++]);
                            int i_905_ = (i_904_ & 0xff0000) * i_899_ & ~0xffffff;
                            int i_906_ = (i_904_ & 0xff00) * i_900_ & 0xff0000;
                            int i_907_ = (i_904_ & 0xff) * i_901_ & 0xff00;
                            is[i_888_++] = (i_905_ | i_906_ | i_907_) >>> 8;
                        }
                        i_888_ += i_892_;
                        i_889_ += i_893_;
                    }
                    return;
                }
                if (i_884_ == 3) {
                    for (int i_908_ = -i_890_; i_908_ < 0; i_908_++) {
                        for (int i_909_ = -i_891_; i_909_ < 0; i_909_++) {
                            int i_910_ = (this.anIntArray9933[i_889_++]);
                            int i_911_ = i_910_ + i_885_;
                            int i_912_ = (i_910_ & 0xff00ff) + (i_885_ & 0xff00ff);
                            int i_913_ = ((i_912_ & 0x1000100) + (i_911_ - i_912_ & 0x10000));
                            is[i_888_++] = i_911_ - i_913_ | i_913_ - (i_913_ >>> 8);
                        }
                        i_888_ += i_892_;
                        i_889_ += i_893_;
                    }
                    return;
                }
                if (i_884_ == 2) {
                    int i_914_ = i_885_ >>> 24;
                    int i_915_ = 256 - i_914_;
                    int i_916_ = (i_885_ & 0xff00ff) * i_915_ & ~0xff00ff;
                    int i_917_ = (i_885_ & 0xff00) * i_915_ & 0xff0000;
                    i_885_ = (i_916_ | i_917_) >>> 8;
                    for (int i_918_ = -i_890_; i_918_ < 0; i_918_++) {
                        for (int i_919_ = -i_891_; i_919_ < 0; i_919_++) {
                            int i_920_ = (this.anIntArray9933[i_889_++]);
                            i_916_ = (i_920_ & 0xff00ff) * i_914_ & ~0xff00ff;
                            i_917_ = (i_920_ & 0xff00) * i_914_ & 0xff0000;
                            is[i_888_++] = ((i_916_ | i_917_) >>> 8) + i_885_;
                        }
                        i_888_ += i_892_;
                        i_889_ += i_893_;
                    }
                    return;
                }
                throw new IllegalArgumentException();
            }
            if (i_886_ == 1) {
                if (i_884_ == 1) {
                    for (int i_921_ = -i_890_; i_921_ < 0; i_921_++) {
                        int i_922_ = i_888_ + i_891_ - 3;
                        while (i_888_ < i_922_) {
                            int i_923_ = (this.anIntArray9933[i_889_++]);
                            if (i_923_ != 0) is[i_888_++] = i_923_;
                            else i_888_++;
                            i_923_ = (this.anIntArray9933[i_889_++]);
                            if (i_923_ != 0) is[i_888_++] = i_923_;
                            else i_888_++;
                            i_923_ = (this.anIntArray9933[i_889_++]);
                            if (i_923_ != 0) is[i_888_++] = i_923_;
                            else i_888_++;
                            i_923_ = (this.anIntArray9933[i_889_++]);
                            if (i_923_ != 0) is[i_888_++] = i_923_;
                            else i_888_++;
                        }
                        i_922_ += 3;
                        while (i_888_ < i_922_) {
                            int i_924_ = (this.anIntArray9933[i_889_++]);
                            if (i_924_ != 0) is[i_888_++] = i_924_;
                            else i_888_++;
                        }
                        i_888_ += i_892_;
                        i_889_ += i_893_;
                    }
                    return;
                }
                if (i_884_ == 0) {
                    if ((i_885_ & 0xffffff) == 16777215) {
                        int i_925_ = i_885_ >>> 24;
                        int i_926_ = 256 - i_925_;
                        for (int i_927_ = -i_890_; i_927_ < 0; i_927_++) {
                            for (int i_928_ = -i_891_; i_928_ < 0; i_928_++) {
                                int i_929_ = (this.anIntArray9933[i_889_++]);
                                if (i_929_ != 0) {
                                    int i_930_ = is[i_888_];
                                    is[i_888_++] = ((((i_929_ & 0xff00ff) * i_925_ + (i_930_ & 0xff00ff) * i_926_) & ~0xff00ff) + (((i_929_ & 0xff00) * i_925_ + (i_930_ & 0xff00) * i_926_) & 0xff0000)) >> 8;
                                } else i_888_++;
                            }
                            i_888_ += i_892_;
                            i_889_ += i_893_;
                        }
                    } else {
                        int i_931_ = (i_885_ & 0xff0000) >> 16;
                        int i_932_ = (i_885_ & 0xff00) >> 8;
                        int i_933_ = i_885_ & 0xff;
                        int i_934_ = i_885_ >>> 24;
                        int i_935_ = 256 - i_934_;
                        for (int i_936_ = -i_890_; i_936_ < 0; i_936_++) {
                            for (int i_937_ = -i_891_; i_937_ < 0; i_937_++) {
                                int i_938_ = (this.anIntArray9933[i_889_++]);
                                if (i_938_ != 0) {
                                    if (i_934_ == 255) {
                                        int i_943_ = ((i_938_ & 0xff0000) * i_931_ & ~0xffffff);
                                        int i_944_ = ((i_938_ & 0xff00) * i_932_ & 0xff0000);
                                        int i_945_ = ((i_938_ & 0xff) * i_933_ & 0xff00);
                                        is[i_888_++] = (i_943_ | i_944_ | i_945_) >>> 8;
                                    } else {
                                        int i_939_ = ((i_938_ & 0xff0000) * i_931_ & ~0xffffff);
                                        int i_940_ = ((i_938_ & 0xff00) * i_932_ & 0xff0000);
                                        int i_941_ = ((i_938_ & 0xff) * i_933_ & 0xff00);
                                        i_938_ = (i_939_ | i_940_ | i_941_) >>> 8;
                                        int i_942_ = is[i_888_];
                                        is[i_888_++] = ((((i_938_ & 0xff00ff) * i_934_ + ((i_942_ & 0xff00ff) * i_935_)) & ~0xff00ff) + (((i_938_ & 0xff00) * i_934_ + ((i_942_ & 0xff00) * i_935_)) & 0xff0000)) >> 8;
                                    }
                                } else i_888_++;
                            }
                            i_888_ += i_892_;
                            i_889_ += i_893_;
                        }
                        return;
                    }
                    return;
                }
                if (i_884_ == 3) {
                    int i_946_ = i_885_ >>> 24;
                    int i_947_ = 256 - i_946_;
                    for (int i_948_ = -i_890_; i_948_ < 0; i_948_++) {
                        for (int i_949_ = -i_891_; i_949_ < 0; i_949_++) {
                            int i_950_ = (this.anIntArray9933[i_889_++]);
                            int i_951_ = i_950_ + i_885_;
                            int i_952_ = (i_950_ & 0xff00ff) + (i_885_ & 0xff00ff);
                            int i_953_ = ((i_952_ & 0x1000100) + (i_951_ - i_952_ & 0x10000));
                            i_953_ = i_951_ - i_953_ | i_953_ - (i_953_ >>> 8);
                            if (i_950_ == 0 && i_946_ != 255) {
                                i_950_ = i_953_;
                                i_953_ = is[i_888_];
                                i_953_ = ((((i_950_ & 0xff00ff) * i_946_ + (i_953_ & 0xff00ff) * i_947_) & ~0xff00ff) + (((i_950_ & 0xff00) * i_946_ + (i_953_ & 0xff00) * i_947_) & 0xff0000)) >> 8;
                            }
                            is[i_888_++] = i_953_;
                        }
                        i_888_ += i_892_;
                        i_889_ += i_893_;
                    }
                    return;
                }
                if (i_884_ == 2) {
                    int i_954_ = i_885_ >>> 24;
                    int i_955_ = 256 - i_954_;
                    int i_956_ = (i_885_ & 0xff00ff) * i_955_ & ~0xff00ff;
                    int i_957_ = (i_885_ & 0xff00) * i_955_ & 0xff0000;
                    i_885_ = (i_956_ | i_957_) >>> 8;
                    for (int i_958_ = -i_890_; i_958_ < 0; i_958_++) {
                        for (int i_959_ = -i_891_; i_959_ < 0; i_959_++) {
                            int i_960_ = (this.anIntArray9933[i_889_++]);
                            if (i_960_ != 0) {
                                i_956_ = (i_960_ & 0xff00ff) * i_954_ & ~0xff00ff;
                                i_957_ = (i_960_ & 0xff00) * i_954_ & 0xff0000;
                                is[i_888_++] = ((i_956_ | i_957_) >>> 8) + i_885_;
                            } else i_888_++;
                        }
                        i_888_ += i_892_;
                        i_889_ += i_893_;
                    }
                    return;
                }
                throw new IllegalArgumentException();
            }
            if (i_886_ == 2) {
                if (i_884_ == 1) {
                    for (int i_961_ = -i_890_; i_961_ < 0; i_961_++) {
                        for (int i_962_ = -i_891_; i_962_ < 0; i_962_++) {
                            int i_963_ = (this.anIntArray9933[i_889_++]);
                            if (i_963_ != 0) {
                                int i_964_ = is[i_888_];
                                int i_965_ = i_963_ + i_964_;
                                int i_966_ = ((i_963_ & 0xff00ff) + (i_964_ & 0xff00ff));
                                i_964_ = (i_966_ & 0x1000100) + (i_965_ - i_966_ & 0x10000);
                                is[i_888_++] = i_965_ - i_964_ | i_964_ - (i_964_ >>> 8);
                            } else i_888_++;
                        }
                        i_888_ += i_892_;
                        i_889_ += i_893_;
                    }
                    return;
                }
                if (i_884_ == 0) {
                    int i_967_ = (i_885_ & 0xff0000) >> 16;
                    int i_968_ = (i_885_ & 0xff00) >> 8;
                    int i_969_ = i_885_ & 0xff;
                    for (int i_970_ = -i_890_; i_970_ < 0; i_970_++) {
                        for (int i_971_ = -i_891_; i_971_ < 0; i_971_++) {
                            int i_972_ = (this.anIntArray9933[i_889_++]);
                            if (i_972_ != 0) {
                                int i_973_ = (i_972_ & 0xff0000) * i_967_ & ~0xffffff;
                                int i_974_ = (i_972_ & 0xff00) * i_968_ & 0xff0000;
                                int i_975_ = (i_972_ & 0xff) * i_969_ & 0xff00;
                                i_972_ = (i_973_ | i_974_ | i_975_) >>> 8;
                                int i_976_ = is[i_888_];
                                int i_977_ = i_972_ + i_976_;
                                int i_978_ = ((i_972_ & 0xff00ff) + (i_976_ & 0xff00ff));
                                i_976_ = (i_978_ & 0x1000100) + (i_977_ - i_978_ & 0x10000);
                                is[i_888_++] = i_977_ - i_976_ | i_976_ - (i_976_ >>> 8);
                            } else i_888_++;
                        }
                        i_888_ += i_892_;
                        i_889_ += i_893_;
                    }
                    return;
                }
                if (i_884_ == 3) {
                    for (int i_979_ = -i_890_; i_979_ < 0; i_979_++) {
                        for (int i_980_ = -i_891_; i_980_ < 0; i_980_++) {
                            int i_981_ = (this.anIntArray9933[i_889_++]);
                            int i_982_ = i_981_ + i_885_;
                            int i_983_ = (i_981_ & 0xff00ff) + (i_885_ & 0xff00ff);
                            int i_984_ = ((i_983_ & 0x1000100) + (i_982_ - i_983_ & 0x10000));
                            i_981_ = i_982_ - i_984_ | i_984_ - (i_984_ >>> 8);
                            i_984_ = is[i_888_];
                            i_982_ = i_981_ + i_984_;
                            i_983_ = (i_981_ & 0xff00ff) + (i_984_ & 0xff00ff);
                            i_984_ = (i_983_ & 0x1000100) + (i_982_ - i_983_ & 0x10000);
                            is[i_888_++] = i_982_ - i_984_ | i_984_ - (i_984_ >>> 8);
                        }
                        i_888_ += i_892_;
                        i_889_ += i_893_;
                    }
                    return;
                }
                if (i_884_ == 2) {
                    int i_985_ = i_885_ >>> 24;
                    int i_986_ = 256 - i_985_;
                    int i_987_ = (i_885_ & 0xff00ff) * i_986_ & ~0xff00ff;
                    int i_988_ = (i_885_ & 0xff00) * i_986_ & 0xff0000;
                    i_885_ = (i_987_ | i_988_) >>> 8;
                    for (int i_989_ = -i_890_; i_989_ < 0; i_989_++) {
                        for (int i_990_ = -i_891_; i_990_ < 0; i_990_++) {
                            int i_991_ = (this.anIntArray9933[i_889_++]);
                            if (i_991_ != 0) {
                                i_987_ = (i_991_ & 0xff00ff) * i_985_ & ~0xff00ff;
                                i_988_ = (i_991_ & 0xff00) * i_985_ & 0xff0000;
                                i_991_ = ((i_987_ | i_988_) >>> 8) + i_885_;
                                int i_992_ = is[i_888_];
                                int i_993_ = i_991_ + i_992_;
                                int i_994_ = ((i_991_ & 0xff00ff) + (i_992_ & 0xff00ff));
                                i_992_ = (i_994_ & 0x1000100) + (i_993_ - i_994_ & 0x10000);
                                is[i_888_++] = i_993_ - i_992_ | i_992_ - (i_992_ >>> 8);
                            } else i_888_++;
                        }
                        i_888_ += i_892_;
                        i_889_ += i_893_;
                    }
                    return;
                }
                throw new IllegalArgumentException();
            }
            throw new IllegalArgumentException();
        }
    }

    final void method979(int i, int i_589_, int i_590_, int i_591_, int i_592_, int i_593_) {
        int[] is = this.aHa_Sub1_8460.anIntArray7483;
        for (int i_594_ = 0; i_594_ < i_591_; i_594_++) {
            int i_595_ = (i_589_ + i_594_) * this.anInt8471 + i;
            int i_596_ = ((i_593_ + i_594_) * this.aHa_Sub1_8460.anInt7477 + i_592_);
            for (int i_597_ = 0; i_597_ < i_590_; i_597_++)
                this.anIntArray9933[i_595_ + i_597_] = is[i_596_ + i_597_];
        }
    }

    final void method968(int i, int i_741_, int i_742_) {
        throw new IllegalStateException("Can't capture alpha into a java_sprite_24");
    }

    // Unreachable per JaCoCo coverage (0 hits) - see Class105_Sub3.method996.
    final void method996(int i, int i_995_, int i_996_, int i_997_, int i_998_, int i_999_, int i_1000_, int i_1001_, int i_1002_) {
        throw new IllegalStateException();
    }

    JavaRgbSprite(JavaToolkit var_ha_Sub1, int i, int i_743_) {
        super(var_ha_Sub1, i, i_743_);
        this.anIntArray9933 = new int[i * i_743_];
    }

    JavaRgbSprite(JavaToolkit var_ha_Sub1, int[] is, int i, int i_744_, int i_745_, int i_746_, boolean bool) {
        super(var_ha_Sub1, i_745_, i_746_);
        if (bool) this.anIntArray9933 = new int[i_745_ * i_746_];
        else this.anIntArray9933 = is;
        i_744_ -= this.anInt8471;
        int i_747_ = 0;
        for (int i_748_ = 0; i_748_ < i_746_; i_748_++) {
            for (int i_749_ = 0; i_749_ < i_745_; i_749_++) {
                int i_750_ = is[i++];
                if (i_750_ >>> 24 == 255) this.anIntArray9933[i_747_++] = (i_750_ & 0xffffff) == 0 ? -16777215 : i_750_;
                else this.anIntArray9933[i_747_++] = 0;
            }
            i += i_744_;
        }
    }

    JavaRgbSprite(JavaToolkit var_ha_Sub1, int[] is, int i, int i_751_) {
        super(var_ha_Sub1, i, i_751_);
        this.anIntArray9933 = is;
    }
}
