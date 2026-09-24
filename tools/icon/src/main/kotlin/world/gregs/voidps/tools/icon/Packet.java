package world.gregs.voidps.tools.icon;/* Class348_Sub49 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

class Packet extends Class348 {
    static int anInt7137;
    static int anInt7141;
    static int anInt7143;
    static int anInt7144;
    static int anInt7150;
    static int anInt7153;
    static int anInt7155;
    static int anInt7158;
    static int anInt7159;
    static int anInt7166;
    static int anInt7186;
    static int anInt7191;
    static int anInt7192;
    static int anInt7196;
    static int anInt7199;
    static int anInt7202;
    static int anInt7203;
    static int anInt7204;
    static int anInt7178;
    byte[] aByteArray7154;
    int pos;

    final void method3367(int i, int[] is, int i_47_, int i_48_) {
        anInt7178++;
        int i_49_ = this.pos;
        this.pos = i_47_;
        int i_50_ = (i_48_ + -i_47_) / 8;
        for (int i_51_ = 0; i_50_ > i_51_; i_51_++) {
            int i_52_ = readInt((byte) -126);
            int i_53_ = readInt((byte) -126);
            int i_54_ = -957401312;
            int i_55_ = -1640531527;
            int i_56_ = 32;
            while (i_56_-- > 0) {
                i_53_ -= ((i_52_ << 4 ^ i_52_ >>> 5) + i_52_ ^ i_54_ - -is[0x4d000003 & i_54_ >>> 11]);
                i_54_ -= i_55_;
                i_52_ -= (i_54_ - -is[i_54_ & 0x3] ^ (i_53_ << 4 ^ i_53_ >>> 5) - -i_53_);
            }
            this.pos -= 8;
            writeInt((byte) 113, i_52_);
            writeInt((byte) 126, i_53_);
        }
        if (i == 607818341) this.pos = i_49_;
    }

    final int readUnsignedShort(int i) {
        if (i != 842397944) return 111;
        this.pos += 2;
        anInt7186++;
        return ((0xff & (this.aByteArray7154[-1 + this.pos])) + ((this.aByteArray7154[-2 + this.pos]) << 8 & 0xff00));
    }

    final int method3362(byte i) {
        anInt7155++;
        int i_43_ = ((this.aByteArray7154[this.pos]) & 0xff);
        if (i != 77) readByte(-48);
        if (i_43_ < 128) return -64 + readUnsignedByte(255);
        return readUnsignedShort(i ^ 0x3235f8b5) - 49152;
    }

    final int readMedium(int i) {
        this.pos += 3;
        anInt7203++;
        if (i != -1) return -52;
        return ((0xff00 & (this.aByteArray7154[-2 + this.pos]) << 8) + ((((this.aByteArray7154[-3 + this.pos]) & 0xff) << 16) - -((this.aByteArray7154[-1 + this.pos]) & 0xff)));
    }

    final int readShort(int i) {
        anInt7204++;
        if (i != 13638) method3350(-23, true, null, -10);
        this.pos += 2;
        int i_65_ = (((this.aByteArray7154[this.pos - 1]) & 0xff) + (((this.aByteArray7154[-2 + this.pos]) & 0xff) << 8));
        if (i_65_ > 32767) i_65_ -= 65536;
        return i_65_;
    }

    final String readString(byte i) {
        anInt7166++;
        int i_68_ = -81 / ((i - 30) / 52);
        int i_69_ = this.pos;
        while ((this.aByteArray7154[this.pos++]) != 0) {
            /* empty */
        }
        int i_70_ = -1 + this.pos - i_69_;
        if (i_70_ == 0) return "";
        return Class367_Sub8.method3546(this.aByteArray7154, 0, i_70_, i_69_);
    }

    final int readUnsignedByte(int i) {
        if (i != 255) writeBytes(-101, 111, null, 33);
        anInt7153++;
        return ((this.aByteArray7154[this.pos++]) & 0xff);
    }

    final byte readByte(int i) {
        if (i >= -75) writeByteAdd((byte) -18, -24);
        anInt7143++;
        return (this.aByteArray7154[this.pos++]);
    }

    final void gdata(int i, int i_82_, int i_83_, byte[] is) {
        anInt7159++;
        for (int i_84_ = i_82_; i_83_ + i_82_ > i_84_; i_84_++)
            is[i_84_] = (this.aByteArray7154[this.pos++]);
        if (i != 2147483647) anInt7207 = -47;
    }

    static int anInt7207;

    Packet(int i) {
        this.pos = 0;
        this.aByteArray7154 = Class37.method359(i, -1);
    }

    Packet(byte[] is) {
        this.aByteArray7154 = is;
        this.pos = 0;
    }

    final int readInt(byte i) {
        anInt7196++;
        this.pos += 4;
        if (i != -126) method3368(-61, -64);
        return ((0xff & (this.aByteArray7154[this.pos - 1])) + ((((this.aByteArray7154[-4 + this.pos]) & 0xff) << 24) + (0xff0000 & ((this.aByteArray7154[-3 + this.pos]) << 16))) - -(((this.aByteArray7154[-2 + this.pos]) & 0xff) << 8));
    }

    final void method3350(int i, boolean bool, int[] is, int i_25_) {
        anInt7137++;
        int i_26_ = this.pos;
        this.pos = i;
        int i_27_ = (-i + i_25_) / 8;
        for (int i_28_ = 0; i_27_ > i_28_; i_28_++) {
            int i_29_ = readInt((byte) -126);
            int i_30_ = readInt((byte) -126);
            int i_31_ = 0;
            int i_32_ = -1640531527;
            int i_33_ = 32;
            while (i_33_-- > 0) {
                i_29_ += (i_31_ - -is[i_31_ & 0x3] ^ (i_30_ >>> 5 ^ i_30_ << 4) - -i_30_);
                i_31_ += i_32_;
                i_30_ += (i_31_ - -is[(0x1a0b & i_31_) >>> 11] ^ i_29_ + (i_29_ >>> 5 ^ i_29_ << 4));
            }
            this.pos -= 8;
            writeInt((byte) 91, i_29_);
            writeInt((byte) 98, i_30_);
        }
        if (bool != true) method3394(88, 83);
        this.pos = i_26_;
    }

    final long method3368(int i, int i_57_) {
        i--;
        anInt7191++;
        if (i < 0 || i > 7) throw new IllegalArgumentException();
        if (i_57_ != 3060) return 99L;
        int i_58_ = 8 * i;
        long l = 0L;
        for (/**/; i_58_ >= 0; i_58_ -= 8)
            l |= ((long) (this.aByteArray7154[this.pos++]) & 0xffL) << i_58_;
        return l;
    }

    final void writeBytes(int i, int i_73_, byte[] is, int i_74_) {
        for (int i_75_ = i_73_; i_73_ + i > i_75_; i_75_++)
            this.aByteArray7154[this.pos++] = is[i_75_];
        int i_76_ = -41 % ((8 - i_74_) / 52);
        anInt7199++;
    }

    final void writeByteAdd(byte i, int i_94_) {
        anInt7192++;
        this.aByteArray7154[this.pos++] = (byte) (i_94_ + 128);
        int i_95_ = -21 % ((-8 - i) / 57);
    }

    final void writeInt(byte i, int i_90_) {
        this.aByteArray7154[this.pos++] = (byte) (i_90_ >> 24);
        if (i < 84) writeByteAdd((byte) -122, -112);
        anInt7202++;
        this.aByteArray7154[this.pos++] = (byte) (i_90_ >> 16);
        this.aByteArray7154[this.pos++] = (byte) (i_90_ >> 8);
        this.aByteArray7154[this.pos++] = (byte) i_90_;
    }

    final void method3394(int i, int i_93_) {
        this.aByteArray7154[this.pos++] = (byte) i_93_;
        anInt7141++;
        this.aByteArray7154[this.pos++] = (byte) (i_93_ >> 8);
        if (i == -23892) {
            this.aByteArray7154[this.pos++] = (byte) (i_93_ >> 16);
            this.aByteArray7154[this.pos++] = (byte) (i_93_ >> 24);
        }
    }
}
