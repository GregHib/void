package world.gregs.voidps.tools.inv.item;/* Class207 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class207 {
    byte[] aByteArray2695;
    int anInt2696;
    int[] anIntArray2697;
    int anInt2698;
    byte[] aByteArray2699;
    int anInt2700;
    int anInt2701;
    int anInt2702;
    int anInt2703;

    final int method1510() {
        return (this.anInt2702 + this.anInt2703 + this.anInt2698);
    }

    static final Class207 method1512(Js5 js5, int i) {
        byte[] is = js5.getFile((byte) 73, i);
        if (is == null) return null;
        return method1517(is)[0];
    }

    static final Class207 method1521(Js5 js5, int i, int i_112_) {
        byte[] is = js5.getFile(-1860, i, i_112_);
        if (is == null) return null;
        return method1517(is)[0];
    }

    final int[] method1516() {
        int i = method1510();
        int[] is = new int[i * method1522()];
        if (this.aByteArray2695 == null) {
            for (int i_67_ = 0; i_67_ < this.anInt2696; i_67_++) {
                int i_68_ = i_67_ * this.anInt2702;
                int i_69_ = (this.anInt2703 + (i_67_ + this.anInt2700) * i);
                for (int i_70_ = 0; i_70_ < this.anInt2702; i_70_++) {
                    int i_71_ = (this.anIntArray2697[this.aByteArray2699[i_68_++] & 0xff]);
                    if (i_71_ != 0) is[i_69_++] = ~0xffffff | i_71_;
                    else is[i_69_++] = 0;
                }
            }
        } else {
            for (int i_63_ = 0; i_63_ < this.anInt2696; i_63_++) {
                int i_64_ = i_63_ * this.anInt2702;
                int i_65_ = (this.anInt2703 + (i_63_ + this.anInt2700) * i);
                for (int i_66_ = 0; i_66_ < this.anInt2702; i_66_++) {
                    is[i_65_++] = (this.aByteArray2695[i_64_] << 24 | (this.anIntArray2697[(this.aByteArray2699[i_64_] & 0xff)]));
                    i_64_++;
                }
            }
        }
        return is;
    }

    private static final Class207[] method1517(byte[] is) {
        Packet packet = new Packet(is);
        packet.pos = is.length - 2;
        int i = packet.readUnsignedShort(842397944);
        Class207[] class207s = new Class207[i];
        for (int i_72_ = 0; i_72_ < i; i_72_++)
            class207s[i_72_] = new Class207();
        packet.pos = is.length - 7 - i * 8;
        int i_73_ = packet.readUnsignedShort(842397944);
        int i_74_ = packet.readUnsignedShort(842397944);
        int i_75_ = (packet.readUnsignedByte(255) & 0xff) + 1;
        for (int i_76_ = 0; i_76_ < i; i_76_++)
            class207s[i_76_].anInt2703 = packet.readUnsignedShort(842397944);
        for (int i_77_ = 0; i_77_ < i; i_77_++)
            class207s[i_77_].anInt2700 = packet.readUnsignedShort(842397944);
        for (int i_78_ = 0; i_78_ < i; i_78_++)
            class207s[i_78_].anInt2702 = packet.readUnsignedShort(842397944);
        for (int i_79_ = 0; i_79_ < i; i_79_++)
            class207s[i_79_].anInt2696 = packet.readUnsignedShort(842397944);
        for (int i_80_ = 0; i_80_ < i; i_80_++) {
            Class207 class207 = class207s[i_80_];
            class207.anInt2698 = (i_73_ - class207.anInt2702 - class207.anInt2703);
            class207.anInt2701 = (i_74_ - class207.anInt2696 - class207.anInt2700);
        }
        packet.pos = is.length - 7 - i * 8 - (i_75_ - 1) * 3;
        int[] is_81_ = new int[i_75_];
        for (int i_82_ = 1; i_82_ < i_75_; i_82_++) {
            is_81_[i_82_] = packet.readMedium(-1);
            if (is_81_[i_82_] == 0) is_81_[i_82_] = 1;
        }
        for (int i_83_ = 0; i_83_ < i; i_83_++)
            class207s[i_83_].anIntArray2697 = is_81_;
        packet.pos = 0;
        for (int i_84_ = 0; i_84_ < i; i_84_++) {
            Class207 class207 = class207s[i_84_];
            int i_85_ = (class207.anInt2702 * class207.anInt2696);
            class207.aByteArray2699 = new byte[i_85_];
            int i_86_ = packet.readUnsignedByte(255);
            if ((i_86_ & 0x2) == 0) {
                if ((i_86_ & 0x1) == 0) {
                    for (int i_87_ = 0; i_87_ < i_85_; i_87_++)
                        class207.aByteArray2699[i_87_] = packet.readByte(-126);
                } else {
                    for (int i_88_ = 0; i_88_ < class207.anInt2702; i_88_++) {
                        for (int i_89_ = 0; i_89_ < class207.anInt2696; i_89_++)
                            class207.aByteArray2699[(i_88_ + i_89_ * class207.anInt2702)] = packet.readByte(-96);
                    }
                }
            } else {
                boolean bool = false;
                class207.aByteArray2695 = new byte[i_85_];
                if ((i_86_ & 0x1) == 0) {
                    for (int i_90_ = 0; i_90_ < i_85_; i_90_++)
                        class207.aByteArray2699[i_90_] = packet.readByte(-118);
                    for (int i_91_ = 0; i_91_ < i_85_; i_91_++) {
                        byte i_92_ = (class207.aByteArray2695[i_91_] = packet.readByte(-89));
                        bool = bool | i_92_ != -1;
                    }
                } else {
                    for (int i_93_ = 0; i_93_ < class207.anInt2702; i_93_++) {
                        for (int i_94_ = 0; i_94_ < class207.anInt2696; i_94_++)
                            class207.aByteArray2699[(i_93_ + i_94_ * class207.anInt2702)] = packet.readByte(-84);
                    }
                    for (int i_95_ = 0; i_95_ < class207.anInt2702; i_95_++) {
                        for (int i_96_ = 0; i_96_ < class207.anInt2696; i_96_++) {
                            byte i_97_ = (class207.aByteArray2695[i_95_ + i_96_ * (class207.anInt2702)] = packet.readByte(-122));
                            bool = bool | i_97_ != -1;
                        }
                    }
                }
                if (!bool) class207.aByteArray2695 = null;
            }
        }
        return class207s;
    }

    public Class207() {
        /* empty */
    }

    final int method1522() {
        return (this.anInt2696 + this.anInt2700 + this.anInt2701);
    }

    final void method1524() {
        int i = method1510();
        int i_113_ = method1522();
        if (this.anInt2702 != i || this.anInt2696 != i_113_) {
            byte[] is = new byte[i * i_113_];
            if (this.aByteArray2695 == null) {
                for (int i_119_ = 0; i_119_ < this.anInt2696; i_119_++) {
                    int i_120_ = i_119_ * this.anInt2702;
                    int i_121_ = ((i_119_ + this.anInt2700) * i + this.anInt2703);
                    for (int i_122_ = 0; i_122_ < this.anInt2702; i_122_++)
                        is[i_121_++] = this.aByteArray2699[i_120_++];
                }
            } else {
                byte[] is_114_ = new byte[i * i_113_];
                for (int i_115_ = 0; i_115_ < this.anInt2696; i_115_++) {
                    int i_116_ = i_115_ * this.anInt2702;
                    int i_117_ = ((i_115_ + this.anInt2700) * i + this.anInt2703);
                    for (int i_118_ = 0; i_118_ < this.anInt2702; i_118_++) {
                        is[i_117_] = this.aByteArray2699[i_116_];
                        is_114_[i_117_++] = this.aByteArray2695[i_116_++];
                    }
                }
                this.aByteArray2695 = is_114_;
            }
            this.anInt2703 = this.anInt2698 = this.anInt2700 = this.anInt2701 = 0;
            this.anInt2702 = i;
            this.anInt2696 = i_113_;
            this.aByteArray2699 = is;
        }
    }
}
