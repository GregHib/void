package world.gregs.voidps.tools.inv.item;/* Class124 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

import java.util.Random;

final class Class124 {
    int[] anIntArray1816;
    int anInt1817;
    int anInt1818;
    static int anInt1819;
    byte[] aByteArray1820;
    int anInt1821;
    short[] aShortArray1822;
    byte[] aByteArray1823;
    int[] anIntArray1824;
    short[] aShortArray1825;
    short[] aShortArray1829;
    int anInt1830 = 12;
    Class162[] aClass162Array1832;
    byte[] aByteArray1833;
    byte[] aByteArray1834;
    short[] aShortArray1835;
    int anInt1836 = 0;
    static int anInt1826;
    static int anInt1828;
    byte[] aByteArray1839;
    int[] anIntArray1841;
    short[] aShortArray1842;
    byte[] aByteArray1843;
    int[] anIntArray1844;
    Class129[] aClass129Array1846;
    int[] anIntArray1847;
    static Class45 aClass45_1848;
    short[] aShortArray1849;
    byte aByte1851;
    int[] anIntArray1852;
    byte[] aByteArray1853;
    short[] aShortArray1855;
    short[] aShortArray1856;
    int[] anIntArray1857;
    int[] anIntArray1859;
    static int anInt1860;
    static int anInt1861;
    short[] aShortArray1862;
    short[] aShortArray1863;
    int[] anIntArray1865;
    Class342[] aClass342Array1866;
    int[] anIntArray1867;
    int[] anIntArray1868;
    static int anInt1869;
    static int anInt1831;
    static int anInt1837;
    static int anInt1854;
    static int anInt1864;
    static int anInt1845;

    final void method1092(int i, int i_0_) {
        anInt1819++;
        for (int i_1_ = 0; this.anInt1836 > i_1_; i_1_++) {
            this.anIntArray1841[i_1_] <<= i;
            this.anIntArray1847[i_1_] <<= i;
            this.anIntArray1852[i_1_] <<= i;
        }
        if (i_0_ <= 39) method1107(40, -7, -80, -24);
        if (this.anInt1818 > 0 && this.anIntArray1859 != null) {
            for (int i_2_ = 0; i_2_ < this.anIntArray1859.length; i_2_++) {
                this.anIntArray1859[i_2_] <<= i;
                this.anIntArray1816[i_2_] <<= i;
                if (this.aByteArray1823[i_2_] != 1) this.anIntArray1844[i_2_] <<= i;
            }
        }
    }

    final int[][] method1093(byte i) {
        anInt1854++;
        int[] is = new int[256];
        int i_3_ = 0;
        for (int i_4_ = 0; this.aClass162Array1832.length > i_4_; i_4_++) {
            int i_5_ = (this.aClass162Array1832[i_4_].anInt2156);
            if (i_5_ >= 0) {
                is[i_5_]++;
                if (i_3_ < i_5_) i_3_ = i_5_;
            }
        }
        int[][] is_6_ = new int[1 + i_3_][];
        for (int i_7_ = 0; i_7_ <= i_3_; i_7_++) {
            is_6_[i_7_] = new int[is[i_7_]];
            is[i_7_] = 0;
        }
        if (i > -68) return null;
        for (int i_8_ = 0; i_8_ < this.aClass162Array1832.length; i_8_++) {
            int i_9_ = (this.aClass162Array1832[i_8_].anInt2156);
            if (i_9_ >= 0) is_6_[i_9_][is[i_9_]++] = i_8_;
        }
        return is_6_;
    }

    final int[][] method1094(byte i) {
        anInt1864++;
        int[] is = new int[256];
        int i_10_ = 0;
        for (int i_11_ = 0; i_11_ < this.anInt1817; i_11_++) {
            int i_12_ = this.anIntArray1824[i_11_];
            if (i_12_ >= 0) {
                if (i_10_ < i_12_) i_10_ = i_12_;
                is[i_12_]++;
            }
        }
        int[][] is_13_ = new int[i_10_ - -1][];
        if (i <= 13) method1102(-65, (byte) -8, (byte) 94, (short) -89, (byte) 6, (short) -117, 21, (byte) 21, -31);
        for (int i_14_ = 0; i_10_ >= i_14_; i_14_++) {
            is_13_[i_14_] = new int[is[i_14_]];
            is[i_14_] = 0;
        }
        for (int i_15_ = 0; i_15_ < this.anInt1817; i_15_++) {
            int i_16_ = this.anIntArray1824[i_15_];
            if (i_16_ >= 0) is_13_[i_16_][is[i_16_]++] = i_15_;
        }
        return is_13_;
    }

    final void method1095(short i, int i_17_, short i_18_) {
        anInt1869++;
        if (this.aShortArray1822 != null) {
            for (int i_19_ = i_17_; this.anInt1817 > i_19_; i_19_++) {
                if (i == this.aShortArray1822[i_19_]) this.aShortArray1822[i_19_] = i_18_;
            }
        }
    }

    public static void method1096(int i) {
        if (i >= 88) aClass45_1848 = null;
    }

    static final int method1097(byte i, int i_20_, Random random) {
        anInt1861++;
        if (i_20_ <= 0) throw new IllegalArgumentException();
        if (Class192.method1436(-19, i_20_)) return (int) ((long) i_20_ * (0xffffffffL & (long) random.nextInt()) >> 32);
        int i_21_ = -2147483648 + -(int) (4294967296L % (long) i_20_);
        if (i < 78) aClass45_1848 = null;
        int i_22_;
        do i_22_ = random.nextInt(); while (i_22_ >= i_21_);
        return Class350.method3452(i_22_, (byte) -15, i_20_);
    }

    final void method1098(short i, byte i_23_, short i_24_) {
        if (i_23_ == 126) {
            for (int i_25_ = 0; i_25_ < this.anInt1817; i_25_++) {
                if (i == this.aShortArray1862[i_25_]) this.aShortArray1862[i_25_] = i_24_;
            }
            anInt1826++;
        }
    }

    private final void method1106(byte[] is, byte i) {
        anInt1828++;
        Class348_Sub49 class348_sub49 = new Class348_Sub49(is);
        Class348_Sub49 class348_sub49_118_ = new Class348_Sub49(is);
        Class348_Sub49 class348_sub49_119_ = new Class348_Sub49(is);
        Class348_Sub49 class348_sub49_120_ = new Class348_Sub49(is);
        Class348_Sub49 class348_sub49_121_ = new Class348_Sub49(is);
        Class348_Sub49 class348_sub49_122_ = new Class348_Sub49(is);
        Class348_Sub49 class348_sub49_123_ = new Class348_Sub49(is);
        class348_sub49.anInt7197 = -23 + is.length;
        this.anInt1836 = class348_sub49.readUnsignedShort(842397944);
        this.anInt1817 = class348_sub49.readUnsignedShort(842397944);
        this.anInt1818 = class348_sub49.readUnsignedByte(255);
        int i_124_ = class348_sub49.readUnsignedByte(255);
        boolean bool = (0x1 & i_124_) == 1;
        boolean bool_125_ = (i_124_ & 0x2) == 2;
        boolean bool_126_ = (i_124_ & 0x4) == 4;
        boolean bool_127_ = (i_124_ & 0x8) == 8;
        if (bool_127_) {
            class348_sub49.anInt7197 -= 7;
            this.anInt1830 = class348_sub49.readUnsignedByte(255);
            class348_sub49.anInt7197 += 6;
        }
        int i_128_ = class348_sub49.readUnsignedByte(255);
        int i_129_ = class348_sub49.readUnsignedByte(255);
        int i_130_ = class348_sub49.readUnsignedByte(255);
        int i_131_ = class348_sub49.readUnsignedByte(255);
        int i_132_ = class348_sub49.readUnsignedByte(255);
        int i_133_ = class348_sub49.readUnsignedShort(842397944);
        int i_134_ = class348_sub49.readUnsignedShort(842397944);
        int i_135_ = class348_sub49.readUnsignedShort(842397944);
        int i_136_ = class348_sub49.readUnsignedShort(842397944);
        int i_137_ = class348_sub49.readUnsignedShort(842397944);
        int i_138_ = 0;
        int i_139_ = 0;
        int i_140_ = 0;
        if (this.anInt1818 > 0) {
            class348_sub49.anInt7197 = 0;
            this.aByteArray1823 = new byte[this.anInt1818];
            for (int i_141_ = 0; (i_141_ < this.anInt1818); i_141_++) {
                byte i_142_ = (this.aByteArray1823[i_141_] = class348_sub49.readByte(-124));
                if (i_142_ >= 1 && i_142_ <= 3) i_139_++;
                if (i_142_ == 2) i_140_++;
                if (i_142_ == 0) i_138_++;
            }
        }
        int i_143_ = this.anInt1818;
        int i_144_ = i_143_;
        i_143_ += this.anInt1836;
        int i_145_ = i_143_;
        if (bool) i_143_ += this.anInt1817;
        int i_146_ = i_143_;
        i_143_ += this.anInt1817;
        int i_147_ = i_143_;
        if (i_128_ == 255) i_143_ += this.anInt1817;
        int i_148_ = i_143_;
        if (i_130_ == 1) i_143_ += this.anInt1817;
        int i_149_ = i_143_;
        if (i_132_ == 1) i_143_ += this.anInt1836;
        int i_150_ = i_143_;
        if (i_129_ == 1) i_143_ += this.anInt1817;
        if (i <= 68) this.anInt1821 = 85;
        int i_151_ = i_143_;
        i_143_ += i_136_;
        int i_152_ = i_143_;
        if (i_131_ == 1) i_143_ += 2 * this.anInt1817;
        int i_153_ = i_143_;
        i_143_ += i_137_;
        int i_154_ = i_143_;
        i_143_ += 2 * this.anInt1817;
        int i_155_ = i_143_;
        i_143_ += i_133_;
        int i_156_ = i_143_;
        i_143_ += i_134_;
        int i_157_ = i_143_;
        i_143_ += i_135_;
        int i_158_ = i_143_;
        i_143_ += i_138_ * 6;
        int i_159_ = i_143_;
        i_143_ += 6 * i_139_;
        int i_160_ = 6;
        if (this.anInt1830 == 14) i_160_ = 7;
        else if (this.anInt1830 >= 15) i_160_ = 9;
        int i_161_ = i_143_;
        i_143_ += i_160_ * i_139_;
        int i_162_ = i_143_;
        i_143_ += i_139_;
        int i_163_ = i_143_;
        i_143_ += i_139_;
        int i_164_ = i_143_;
        i_143_ += 2 * i_140_ + i_139_;
        this.aShortArray1835 = new short[this.anInt1817];
        this.aShortArray1862 = new short[this.anInt1817];
        if (i_130_ == 1) this.anIntArray1824 = new int[this.anInt1817];
        if (this.anInt1818 > 0) {
            if (i_140_ > 0) {
                this.anIntArray1857 = new int[i_140_];
                this.anIntArray1865 = new int[i_140_];
            }
            this.aShortArray1849 = new short[this.anInt1818];
            this.aShortArray1829 = new short[this.anInt1818];
            if (i_139_ > 0) {
                this.anIntArray1867 = new int[i_139_];
                this.aByteArray1853 = new byte[i_139_];
                this.anIntArray1816 = new int[i_139_];
                this.aByteArray1833 = new byte[i_139_];
                this.anIntArray1844 = new int[i_139_];
                this.anIntArray1859 = new int[i_139_];
            }
            this.aShortArray1825 = new short[this.anInt1818];
        }
        if (bool) this.aByteArray1843 = new byte[this.anInt1817];
        if (i_129_ == 1) this.aByteArray1834 = new byte[this.anInt1817];
        if (i_128_ == 255) this.aByteArray1839 = new byte[this.anInt1817];
        else this.aByte1851 = (byte) i_128_;
        this.aShortArray1855 = new short[this.anInt1817];
        this.anIntArray1847 = new int[this.anInt1836];
        this.aShortArray1863 = new short[this.anInt1817];
        if (i_131_ == 1 && this.anInt1818 > 0) this.aByteArray1820 = new byte[this.anInt1817];
        int i_165_ = i_143_;
        this.anIntArray1852 = new int[this.anInt1836];
        this.anIntArray1841 = new int[this.anInt1836];
        if (i_132_ == 1) this.anIntArray1868 = new int[this.anInt1836];
        class348_sub49.anInt7197 = i_144_;
        if (i_131_ == 1) this.aShortArray1822 = new short[this.anInt1817];
        class348_sub49_118_.anInt7197 = i_155_;
        class348_sub49_119_.anInt7197 = i_156_;
        class348_sub49_120_.anInt7197 = i_157_;
        class348_sub49_121_.anInt7197 = i_149_;
        int i_166_ = 0;
        int i_167_ = 0;
        int i_168_ = 0;
        for (int i_169_ = 0; this.anInt1836 > i_169_; i_169_++) {
            int i_170_ = class348_sub49.readUnsignedByte(255);
            int i_171_ = 0;
            if ((i_170_ & 0x1) != 0) i_171_ = class348_sub49_118_.method3362((byte) 77);
            int i_172_ = 0;
            if ((i_170_ & 0x2) != 0) i_172_ = class348_sub49_119_.method3362((byte) 77);
            int i_173_ = 0;
            if ((0x4 & i_170_) != 0) i_173_ = class348_sub49_120_.method3362((byte) 77);
            this.anIntArray1841[i_169_] = i_171_ + i_166_;
            this.anIntArray1847[i_169_] = i_172_ + i_167_;
            this.anIntArray1852[i_169_] = i_168_ + i_173_;
            i_167_ = this.anIntArray1847[i_169_];
            i_166_ = this.anIntArray1841[i_169_];
            i_168_ = this.anIntArray1852[i_169_];
            if (i_132_ == 1) this.anIntArray1868[i_169_] = class348_sub49_121_.readUnsignedByte(255);
        }
        class348_sub49.anInt7197 = i_154_;
        class348_sub49_118_.anInt7197 = i_145_;
        class348_sub49_119_.anInt7197 = i_147_;
        class348_sub49_120_.anInt7197 = i_150_;
        class348_sub49_121_.anInt7197 = i_148_;
        class348_sub49_122_.anInt7197 = i_152_;
        class348_sub49_123_.anInt7197 = i_153_;
        for (int i_174_ = 0; (this.anInt1817 > i_174_); i_174_++) {
            this.aShortArray1862[i_174_] = (short) class348_sub49.readUnsignedShort(842397944);
            if (bool) this.aByteArray1843[i_174_] = class348_sub49_118_.readByte(-98);
            if (i_128_ == 255) this.aByteArray1839[i_174_] = class348_sub49_119_.readByte(-78);
            if (i_129_ == 1) this.aByteArray1834[i_174_] = class348_sub49_120_.readByte(-99);
            if (i_130_ == 1) this.anIntArray1824[i_174_] = class348_sub49_121_.readUnsignedByte(255);
            if (i_131_ == 1) this.aShortArray1822[i_174_] = (short) (class348_sub49_122_.readUnsignedShort(842397944) + -1);
            if (this.aByteArray1820 != null) {
                if (this.aShortArray1822[i_174_] == -1) this.aByteArray1820[i_174_] = (byte) -1;
                else this.aByteArray1820[i_174_] = (byte) (-1 + class348_sub49_123_.readUnsignedByte(255));
            }
        }
        class348_sub49.anInt7197 = i_151_;
        this.anInt1821 = -1;
        class348_sub49_118_.anInt7197 = i_146_;
        short i_175_ = 0;
        short i_176_ = 0;
        short i_177_ = 0;
        int i_178_ = 0;
        for (int i_179_ = 0; (i_179_ < this.anInt1817); i_179_++) {
            int i_180_ = class348_sub49_118_.readUnsignedByte(255);
            if (i_180_ == 1) {
                i_175_ = (short) (class348_sub49.method3362((byte) 77) + i_178_);
                i_178_ = i_175_;
                i_176_ = (short) (class348_sub49.method3362((byte) 77) + i_178_);
                i_178_ = i_176_;
                i_177_ = (short) (i_178_ + class348_sub49.method3362((byte) 77));
                i_178_ = i_177_;
                this.aShortArray1863[i_179_] = i_175_;
                this.aShortArray1835[i_179_] = i_176_;
                this.aShortArray1855[i_179_] = i_177_;
                if (i_175_ > this.anInt1821) this.anInt1821 = i_175_;
                if (this.anInt1821 < i_176_) this.anInt1821 = i_176_;
                if (i_177_ > this.anInt1821) this.anInt1821 = i_177_;
            }
            if (i_180_ == 2) {
                i_176_ = i_177_;
                i_177_ = (short) (class348_sub49.method3362((byte) 77) + i_178_);
                this.aShortArray1863[i_179_] = i_175_;
                i_178_ = i_177_;
                this.aShortArray1835[i_179_] = i_176_;
                this.aShortArray1855[i_179_] = i_177_;
                if (this.anInt1821 < i_177_) this.anInt1821 = i_177_;
            }
            if (i_180_ == 3) {
                i_175_ = i_177_;
                i_177_ = (short) (class348_sub49.method3362((byte) 77) + i_178_);
                i_178_ = i_177_;
                this.aShortArray1863[i_179_] = i_175_;
                this.aShortArray1835[i_179_] = i_176_;
                this.aShortArray1855[i_179_] = i_177_;
                if (this.anInt1821 < i_177_) this.anInt1821 = i_177_;
            }
            if (i_180_ == 4) {
                short i_181_ = i_175_;
                i_175_ = i_176_;
                i_177_ = (short) (i_178_ + class348_sub49.method3362((byte) 77));
                i_176_ = i_181_;
                this.aShortArray1863[i_179_] = i_175_;
                i_178_ = i_177_;
                this.aShortArray1835[i_179_] = i_176_;
                this.aShortArray1855[i_179_] = i_177_;
                if (this.anInt1821 < i_177_) this.anInt1821 = i_177_;
            }
        }
        class348_sub49.anInt7197 = i_158_;
        this.anInt1821++;
        class348_sub49_118_.anInt7197 = i_159_;
        class348_sub49_119_.anInt7197 = i_161_;
        class348_sub49_120_.anInt7197 = i_162_;
        class348_sub49_121_.anInt7197 = i_163_;
        class348_sub49_122_.anInt7197 = i_164_;
        for (int i_182_ = 0; this.anInt1818 > i_182_; i_182_++) {
            int i_183_ = this.aByteArray1823[i_182_] & 0xff;
            if (i_183_ == 0) {
                this.aShortArray1829[i_182_] = (short) class348_sub49.readUnsignedShort(842397944);
                this.aShortArray1849[i_182_] = (short) class348_sub49.readUnsignedShort(842397944);
                this.aShortArray1825[i_182_] = (short) class348_sub49.readUnsignedShort(842397944);
            }
            if (i_183_ == 1) {
                this.aShortArray1829[i_182_] = (short) class348_sub49_118_.readUnsignedShort(842397944);
                this.aShortArray1849[i_182_] = (short) class348_sub49_118_.readUnsignedShort(842397944);
                this.aShortArray1825[i_182_] = (short) class348_sub49_118_.readUnsignedShort(842397944);
                if (this.anInt1830 >= 15) {
                    this.anIntArray1859[i_182_] = class348_sub49_119_.readMedium(-1);
                    this.anIntArray1816[i_182_] = class348_sub49_119_.readMedium(-1);
                    this.anIntArray1844[i_182_] = class348_sub49_119_.readMedium(-1);
                } else {
                    this.anIntArray1859[i_182_] = class348_sub49_119_.readUnsignedShort(842397944);
                    if (this.anInt1830 >= 14) this.anIntArray1816[i_182_] = class348_sub49_119_.readMedium(-1);
                    else this.anIntArray1816[i_182_] = class348_sub49_119_.readUnsignedShort(842397944);
                    this.anIntArray1844[i_182_] = class348_sub49_119_.readUnsignedShort(842397944);
                }
                this.aByteArray1833[i_182_] = class348_sub49_120_.readByte(-86);
                this.aByteArray1853[i_182_] = class348_sub49_121_.readByte(-116);
                this.anIntArray1867[i_182_] = class348_sub49_122_.readByte(-79);
            }
            if (i_183_ == 2) {
                this.aShortArray1829[i_182_] = (short) class348_sub49_118_.readUnsignedShort(842397944);
                this.aShortArray1849[i_182_] = (short) class348_sub49_118_.readUnsignedShort(842397944);
                this.aShortArray1825[i_182_] = (short) class348_sub49_118_.readUnsignedShort(842397944);
                if (this.anInt1830 >= 15) {
                    this.anIntArray1859[i_182_] = class348_sub49_119_.readMedium(-1);
                    this.anIntArray1816[i_182_] = class348_sub49_119_.readMedium(-1);
                    this.anIntArray1844[i_182_] = class348_sub49_119_.readMedium(-1);
                } else {
                    this.anIntArray1859[i_182_] = class348_sub49_119_.readUnsignedShort(842397944);
                    if (this.anInt1830 < 14) this.anIntArray1816[i_182_] = class348_sub49_119_.readUnsignedShort(842397944);
                    else this.anIntArray1816[i_182_] = class348_sub49_119_.readMedium(-1);
                    this.anIntArray1844[i_182_] = class348_sub49_119_.readUnsignedShort(842397944);
                }
                this.aByteArray1833[i_182_] = class348_sub49_120_.readByte(-97);
                this.aByteArray1853[i_182_] = class348_sub49_121_.readByte(-100);
                this.anIntArray1867[i_182_] = class348_sub49_122_.readByte(-124);
                this.anIntArray1857[i_182_] = class348_sub49_122_.readByte(-112);
                this.anIntArray1865[i_182_] = class348_sub49_122_.readByte(-114);
            }
            if (i_183_ == 3) {
                this.aShortArray1829[i_182_] = (short) class348_sub49_118_.readUnsignedShort(842397944);
                this.aShortArray1849[i_182_] = (short) class348_sub49_118_.readUnsignedShort(842397944);
                this.aShortArray1825[i_182_] = (short) class348_sub49_118_.readUnsignedShort(842397944);
                if (this.anInt1830 < 15) {
                    this.anIntArray1859[i_182_] = class348_sub49_119_.readUnsignedShort(842397944);
                    if (this.anInt1830 < 14) this.anIntArray1816[i_182_] = class348_sub49_119_.readUnsignedShort(842397944);
                    else this.anIntArray1816[i_182_] = class348_sub49_119_.readMedium(-1);
                    this.anIntArray1844[i_182_] = class348_sub49_119_.readUnsignedShort(842397944);
                } else {
                    this.anIntArray1859[i_182_] = class348_sub49_119_.readMedium(-1);
                    this.anIntArray1816[i_182_] = class348_sub49_119_.readMedium(-1);
                    this.anIntArray1844[i_182_] = class348_sub49_119_.readMedium(-1);
                }
                this.aByteArray1833[i_182_] = class348_sub49_120_.readByte(-104);
                this.aByteArray1853[i_182_] = class348_sub49_121_.readByte(-127);
                this.anIntArray1867[i_182_] = class348_sub49_122_.readByte(-109);
            }
        }
        class348_sub49.anInt7197 = i_165_;
        if (bool_125_) {
            int i_184_ = class348_sub49.readUnsignedByte(255);
            if (i_184_ > 0) {
                this.aClass129Array1846 = new Class129[i_184_];
                for (int i_185_ = 0; i_184_ > i_185_; i_185_++) {
                    int i_186_ = class348_sub49.readUnsignedShort(842397944);
                    int i_187_ = class348_sub49.readUnsignedShort(842397944);
                    byte i_188_;
                    if (i_128_ != 255) i_188_ = (byte) i_128_;
                    else i_188_ = this.aByteArray1839[i_187_];
                    this.aClass129Array1846[i_185_] = (new Class129(i_186_, this.aShortArray1863[i_187_], this.aShortArray1835[i_187_], this.aShortArray1855[i_187_], i_188_));
                }
            }
            int i_189_ = class348_sub49.readUnsignedByte(255);
            if (i_189_ > 0) {
                this.aClass342Array1866 = new Class342[i_189_];
                for (int i_190_ = 0; i_189_ > i_190_; i_190_++) {
                    int i_191_ = class348_sub49.readUnsignedShort(842397944);
                    int i_192_ = class348_sub49.readUnsignedShort(842397944);
                    this.aClass342Array1866[i_190_] = new Class342(i_191_, i_192_);
                }
            }
        }
        if (bool_126_) {
            int i_193_ = class348_sub49.readUnsignedByte(255);
            if (i_193_ > 0) {
                this.aClass162Array1832 = new Class162[i_193_];
                for (int i_194_ = 0; i_193_ > i_194_; i_194_++) {
                    int i_195_ = class348_sub49.readUnsignedShort(842397944);
                    int i_196_ = class348_sub49.readUnsignedShort(842397944);
                    int i_197_ = class348_sub49.readUnsignedByte(255);
                    byte i_198_ = class348_sub49.readByte(-127);
                    this.aClass162Array1832[i_194_] = new Class162(i_195_, i_196_, i_197_, i_198_);
                }
            }
        }
    }

    /* NOTE: method1103 and method1107 are not in the genuine-methods list
     * (only method1106 is exercised by real .java icon data - method1103 is
     * an alternate/legacy decode format never hit, and method1107's caller
     * guard "if (i_0_ <= 39)" in method1092 is never true for reachable
     * callers). Both are added back verbatim anyway since Java requires
     * them to resolve at compile time (kept code still calls them). */
    final int[][] method1100(boolean bool, int i) {
        anInt1845++;
        int[] is = new int[256];
        int i_31_ = 0;
        int i_32_ = (!bool ? this.anInt1821 : this.anInt1836);
        for (int i_33_ = 0; i_33_ < i_32_; i_33_++) {
            int i_34_ = this.anIntArray1868[i_33_];
            if (i_34_ >= 0) {
                is[i_34_]++;
                if (i_31_ < i_34_) i_31_ = i_34_;
            }
        }
        int[][] is_35_ = new int[1 + i_31_][];
        for (int i_36_ = 0; i_36_ <= i_31_; i_36_++) {
            is_35_[i_36_] = new int[is[i_36_]];
            is[i_36_] = 0;
        }
        for (int i_37_ = 0; i_32_ > i_37_; i_37_++) {
            int i_38_ = this.anIntArray1868[i_37_];
            if (i_38_ >= 0) is_35_[i_38_][is[i_38_]++] = i_37_;
        }
        if (i > -14) method1098((short) 9, (byte) 80, (short) -118);
        return is_35_;
    }

    private final int method1102(int i, byte i_44_, byte i_45_, short i_46_, byte i_47_, short i_48_, int i_49_, byte i_50_, int i_51_) {
        this.aShortArray1863[this.anInt1817] = (short) i;
        anInt1860++;
        this.aShortArray1835[this.anInt1817] = (short) i_49_;
        this.aShortArray1855[this.anInt1817] = (short) i_51_;
        if (i_47_ < 30) return -92;
        this.aByteArray1843[this.anInt1817] = i_44_;
        this.aByteArray1820[this.anInt1817] = i_45_;
        this.aShortArray1862[this.anInt1817] = i_48_;
        this.aByteArray1834[this.anInt1817] = i_50_;
        this.aShortArray1822[this.anInt1817] = i_46_;
        return this.anInt1817++;
    }

    private final void method1103(int i, byte[] is) {
        anInt1831++;
        boolean bool = false;
        boolean bool_52_ = false;
        Class348_Sub49 class348_sub49 = new Class348_Sub49(is);
        Class348_Sub49 class348_sub49_53_ = new Class348_Sub49(is);
        Class348_Sub49 class348_sub49_54_ = new Class348_Sub49(is);
        Class348_Sub49 class348_sub49_55_ = new Class348_Sub49(is);
        Class348_Sub49 class348_sub49_56_ = new Class348_Sub49(is);
        class348_sub49.anInt7197 = -18 + is.length;
        this.anInt1836 = class348_sub49.readUnsignedShort(i ^ 0x3235f8f9);
        this.anInt1817 = class348_sub49.readUnsignedShort(842397944);
        this.anInt1818 = class348_sub49.readUnsignedByte(255);
        int i_57_ = class348_sub49.readUnsignedByte(255);
        int i_58_ = class348_sub49.readUnsignedByte(255);
        int i_59_ = class348_sub49.readUnsignedByte(255);
        int i_60_ = class348_sub49.readUnsignedByte(255);
        int i_61_ = class348_sub49.readUnsignedByte(255);
        int i_62_ = class348_sub49.readUnsignedShort(842397944);
        int i_63_ = class348_sub49.readUnsignedShort(842397944);
        int i_64_ = class348_sub49.readUnsignedShort(842397944);
        int i_65_ = class348_sub49.readUnsignedShort(842397944);
        int i_66_ = 0;
        int i_67_ = i_66_;
        i_66_ += this.anInt1836;
        int i_68_ = i_66_;
        i_66_ += this.anInt1817;
        int i_69_ = i_66_;
        if (i_58_ == 255) i_66_ += this.anInt1817;
        int i_70_ = i_66_;
        if (i_60_ == 1) i_66_ += this.anInt1817;
        int i_71_ = i_66_;
        if (i_57_ == 1) i_66_ += this.anInt1817;
        int i_72_ = i_66_;
        if (i_61_ == 1) i_66_ += this.anInt1836;
        int i_73_ = i_66_;
        if (i == i_59_) i_66_ += this.anInt1817;
        int i_74_ = i_66_;
        i_66_ += i_65_;
        int i_75_ = i_66_;
        i_66_ += this.anInt1817 * 2;
        int i_76_ = i_66_;
        i_66_ += this.anInt1818 * 6;
        int i_77_ = i_66_;
        i_66_ += i_62_;
        int i_78_ = i_66_;
        i_66_ += i_63_;
        int i_79_ = i_66_;
        this.aShortArray1862 = new short[this.anInt1817];
        if (i_58_ == 255) this.aByteArray1839 = new byte[this.anInt1817];
        else this.aByte1851 = (byte) i_58_;
        if (i_61_ == 1) this.anIntArray1868 = new int[this.anInt1836];
        if (i_59_ == 1) this.aByteArray1834 = new byte[this.anInt1817];
        this.aShortArray1863 = new short[this.anInt1817];
        if (i_57_ == 1) {
            this.aShortArray1822 = new short[this.anInt1817];
            this.aByteArray1820 = new byte[this.anInt1817];
            this.aByteArray1843 = new byte[this.anInt1817];
        }
        this.anIntArray1847 = new int[this.anInt1836];
        if (i_60_ == 1) this.anIntArray1824 = new int[this.anInt1817];
        this.aShortArray1855 = new short[this.anInt1817];
        if (this.anInt1818 > 0) {
            this.aByteArray1823 = new byte[this.anInt1818];
            this.aShortArray1829 = new short[this.anInt1818];
            this.aShortArray1849 = new short[this.anInt1818];
            this.aShortArray1825 = new short[this.anInt1818];
        }
        this.aShortArray1835 = new short[this.anInt1817];
        this.anIntArray1852 = new int[this.anInt1836];
        this.anIntArray1841 = new int[this.anInt1836];
        class348_sub49.anInt7197 = i_67_;
        i_66_ += i_64_;
        class348_sub49_53_.anInt7197 = i_77_;
        class348_sub49_54_.anInt7197 = i_78_;
        class348_sub49_55_.anInt7197 = i_79_;
        class348_sub49_56_.anInt7197 = i_72_;
        int i_80_ = 0;
        int i_81_ = 0;
        int i_82_ = 0;
        for (int i_83_ = 0; i_83_ < this.anInt1836; i_83_++) {
            int i_84_ = class348_sub49.readUnsignedByte(255);
            int i_85_ = 0;
            if ((i_84_ & 0x1) != 0) i_85_ = class348_sub49_53_.method3362((byte) 77);
            int i_86_ = 0;
            if ((i_84_ & 0x2) != 0) i_86_ = class348_sub49_54_.method3362((byte) 77);
            int i_87_ = 0;
            if ((0x4 & i_84_) != 0) i_87_ = class348_sub49_55_.method3362((byte) 77);
            this.anIntArray1841[i_83_] = i_85_ + i_80_;
            this.anIntArray1847[i_83_] = i_81_ - -i_86_;
            this.anIntArray1852[i_83_] = i_82_ + i_87_;
            i_82_ = this.anIntArray1852[i_83_];
            i_81_ = this.anIntArray1847[i_83_];
            i_80_ = this.anIntArray1841[i_83_];
            if (i_61_ == 1) this.anIntArray1868[i_83_] = class348_sub49_56_.readUnsignedByte(Class348_Sub21.method2955(i, 254));
        }
        class348_sub49.anInt7197 = i_75_;
        class348_sub49_53_.anInt7197 = i_71_;
        class348_sub49_54_.anInt7197 = i_69_;
        class348_sub49_55_.anInt7197 = i_73_;
        class348_sub49_56_.anInt7197 = i_70_;
        for (int i_88_ = 0; i_88_ < this.anInt1817; i_88_++) {
            this.aShortArray1862[i_88_] = (short) class348_sub49.readUnsignedShort(842397944);
            if (i_57_ == 1) {
                int i_89_ = class348_sub49_53_.readUnsignedByte(255);
                if ((0x1 & i_89_) == 1) {
                    this.aByteArray1843[i_88_] = (byte) 1;
                    bool = true;
                } else this.aByteArray1843[i_88_] = (byte) 0;
                if ((i_89_ & 0x2) == 2) {
                    this.aByteArray1820[i_88_] = (byte) (i_89_ >> 2);
                    this.aShortArray1822[i_88_] = this.aShortArray1862[i_88_];
                    this.aShortArray1862[i_88_] = (short) 127;
                    if (this.aShortArray1822[i_88_] != -1) bool_52_ = true;
                } else {
                    this.aByteArray1820[i_88_] = (byte) -1;
                    this.aShortArray1822[i_88_] = (short) -1;
                }
            }
            if (i_58_ == 255) this.aByteArray1839[i_88_] = class348_sub49_54_.readByte(-108);
            if (i_59_ == 1) this.aByteArray1834[i_88_] = class348_sub49_55_.readByte(Class348_Sub21.method2955(i, -120));
            if (i_60_ == 1) this.anIntArray1824[i_88_] = class348_sub49_56_.readUnsignedByte(255);
        }
        class348_sub49.anInt7197 = i_74_;
        this.anInt1821 = -1;
        class348_sub49_53_.anInt7197 = i_68_;
        short i_90_ = 0;
        short i_91_ = 0;
        short i_92_ = 0;
        int i_93_ = 0;
        for (int i_94_ = 0; i_94_ < this.anInt1817; i_94_++) {
            int i_95_ = class348_sub49_53_.readUnsignedByte(255);
            if (i_95_ == 1) {
                i_90_ = (short) (i_93_ + class348_sub49.method3362((byte) 77));
                i_93_ = i_90_;
                i_91_ = (short) (class348_sub49.method3362((byte) 77) + i_93_);
                i_93_ = i_91_;
                i_92_ = (short) (i_93_ + class348_sub49.method3362((byte) 77));
                this.aShortArray1863[i_94_] = i_90_;
                i_93_ = i_92_;
                this.aShortArray1835[i_94_] = i_91_;
                this.aShortArray1855[i_94_] = i_92_;
                if (i_90_ > this.anInt1821) this.anInt1821 = i_90_;
                if (this.anInt1821 < i_91_) this.anInt1821 = i_91_;
                if (this.anInt1821 < i_92_) this.anInt1821 = i_92_;
            }
            if (i_95_ == 2) {
                i_91_ = i_92_;
                i_92_ = (short) (class348_sub49.method3362((byte) 77) + i_93_);
                this.aShortArray1863[i_94_] = i_90_;
                i_93_ = i_92_;
                this.aShortArray1835[i_94_] = i_91_;
                this.aShortArray1855[i_94_] = i_92_;
                if (i_92_ > this.anInt1821) this.anInt1821 = i_92_;
            }
            if (i_95_ == 3) {
                i_90_ = i_92_;
                i_92_ = (short) (class348_sub49.method3362((byte) 77) + i_93_);
                i_93_ = i_92_;
                this.aShortArray1863[i_94_] = i_90_;
                this.aShortArray1835[i_94_] = i_91_;
                this.aShortArray1855[i_94_] = i_92_;
                if (this.anInt1821 < i_92_) this.anInt1821 = i_92_;
            }
            if (i_95_ == 4) {
                short i_96_ = i_90_;
                i_90_ = i_91_;
                i_92_ = (short) (class348_sub49.method3362((byte) 77) + i_93_);
                i_91_ = i_96_;
                i_93_ = i_92_;
                this.aShortArray1863[i_94_] = i_90_;
                this.aShortArray1835[i_94_] = i_91_;
                this.aShortArray1855[i_94_] = i_92_;
                if (this.anInt1821 < i_92_) this.anInt1821 = i_92_;
            }
        }
        this.anInt1821++;
        class348_sub49.anInt7197 = i_76_;
        for (int i_97_ = 0; i_97_ < this.anInt1818; i_97_++) {
            this.aByteArray1823[i_97_] = (byte) 0;
            this.aShortArray1829[i_97_] = (short) class348_sub49.readUnsignedShort(842397944);
            this.aShortArray1849[i_97_] = (short) class348_sub49.readUnsignedShort(842397944);
            this.aShortArray1825[i_97_] = (short) class348_sub49.readUnsignedShort(842397944);
        }
        if (this.aByteArray1820 != null) {
            boolean bool_98_ = false;
            for (int i_99_ = 0; i_99_ < this.anInt1817; i_99_++) {
                int i_100_ = this.aByteArray1820[i_99_] & 0xff;
                if (i_100_ != 255) {
                    if (((0xffff & this.aShortArray1829[i_100_]) != this.aShortArray1863[i_99_]) || (this.aShortArray1835[i_99_] != (0xffff & this.aShortArray1849[i_100_])) || ((0xffff & this.aShortArray1825[i_100_]) != this.aShortArray1855[i_99_])) bool_98_ = true;
                    else this.aByteArray1820[i_99_] = (byte) -1;
                }
            }
            if (!bool_98_) this.aByteArray1820 = null;
        }
        if (!bool) this.aByteArray1843 = null;
        if (!bool_52_) this.aShortArray1822 = null;
    }

    final void method1107(int i, int i_199_, int i_200_, int i_201_) {
        if (i_200_ != 0) {
            int i_202_ = Class70.anIntArray1207[i_200_];
            int i_203_ = Class70.anIntArray1204[i_200_];
            for (int i_204_ = 0; i_204_ < this.anInt1836; i_204_++) {
                int i_205_ = ((i_203_ * this.anIntArray1841[i_204_] + this.anIntArray1847[i_204_] * i_202_) >> 14);
                this.anIntArray1847[i_204_] = ((-(this.anIntArray1841[i_204_] * i_202_) + this.anIntArray1847[i_204_] * i_203_) >> 14);
                this.anIntArray1841[i_204_] = i_205_;
            }
        }
        if (i != 6875) this.aShortArray1835 = null;
        anInt1837++;
        if (i_201_ != 0) {
            int i_206_ = Class70.anIntArray1207[i_201_];
            int i_207_ = Class70.anIntArray1204[i_201_];
            for (int i_208_ = 0; i_208_ < this.anInt1836; i_208_++) {
                int i_209_ = ((this.anIntArray1847[i_208_] * i_207_ + -(i_206_ * this.anIntArray1852[i_208_])) >> 14);
                this.anIntArray1852[i_208_] = ((this.anIntArray1847[i_208_] * i_206_ - -(i_207_ * this.anIntArray1852[i_208_])) >> 14);
                this.anIntArray1847[i_208_] = i_209_;
            }
        }
        if (i_199_ != 0) {
            int i_210_ = Class70.anIntArray1207[i_199_];
            int i_211_ = Class70.anIntArray1204[i_199_];
            for (int i_212_ = 0; this.anInt1836 > i_212_; i_212_++) {
                int i_213_ = ((i_211_ * this.anIntArray1841[i_212_] + this.anIntArray1852[i_212_] * i_210_) >> 14);
                this.anIntArray1852[i_212_] = ((i_211_ * this.anIntArray1852[i_212_] + -(this.anIntArray1841[i_212_] * i_210_)) >> 14);
                this.anIntArray1841[i_212_] = i_213_;
            }
        }
    }

    Class124(byte[] is) {
        this.anInt1817 = 0;
        this.aByte1851 = (byte) 0;
        this.anInt1821 = 0;
        this.anInt1818 = 0;
        if (is[is.length + -1] == -1 && is[-2 + is.length] == -1) method1106(is, (byte) 93);
        else method1103(1, is);
    }

    // Dual-model merge constructor: unreachable per JaCoCo coverage (0 hits)
    // for every item dumped by the real renderer, so trimmed to a stub that
    // only satisfies the type contract (Class213.method1562's i_1_ != -1
    // branch is never taken for the items in this cache).
    Class124(Class124[] class124s, int i) {
        /* empty - see comment above */
    }
}
