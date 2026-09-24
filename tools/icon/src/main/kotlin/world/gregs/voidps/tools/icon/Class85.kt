package world.gregs.voidps.tools.icon;/* Class85 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 *
 * Trimmed: full Whirlpool-style digest implementation kept (needed by
 * Class291's constructor to verify a 64-byte checksum against reference
 * table data - genuine per jacoco: Class291(byte[], int, byte[])).
 */

final class Class85 {
    private int anInt1463;
    private final byte[] aByteArray1465 = new byte[32];
    private int anInt1468;
    private final long[] aLongArray1469;
    private final long[] aLongArray1471 = new long[8];
    private final long[] aLongArray1472;
    private final long[] aLongArray1473;
    private final long[] aLongArray1474;
    private final byte[] aByteArray1475;

    private final void method827(int i) {
        int i_0_ = 0;
        int i_1_ = i;
        for (/**/; i_0_ < 8; i_0_++) {
            aLongArray1473[i_0_] = (Class105_Sub2.method993((Class105_Sub2.method993((Class348_Sub8.method2777(255L, aByteArray1475[i_1_ + 6]) << 8), (Class105_Sub2.method993(Class348_Sub8.method2777(((long) (aByteArray1475[i_1_ - -5]) << 16), 255L << 16), (Class105_Sub2.method993((Class105_Sub2.method993(Class348_Sub8.method2777(255L << 32, ((long) (aByteArray1475[i_1_ + 3]) << 32)), (Class105_Sub2.method993(Class105_Sub2.method993((Class348_Sub8.method2777(255L << 48, ((long) (aByteArray1475[1 + i_1_]) << 48))), ((long) (aByteArray1475[i_1_]) << 56)), Class348_Sub8.method2777(((long) (aByteArray1475[i_1_ + 2]) << 40), 255L << 40))))), Class348_Sub8.method2777(aByteArray1475[4 + i_1_], 255L) << 24)))))), Class348_Sub8.method2777(255L, aByteArray1475[i_1_ - -7])));
            i_1_ += 8;
        }
        for (int i_2_ = 0; i_2_ < 8; i_2_++)
            aLongArray1469[i_2_] = Class105_Sub2.method993(aLongArray1473[i_2_], (aLongArray1472[i_2_] = aLongArray1474[i_2_]));
        for (int i_3_ = 1; i_3_ <= 10; i_3_++) {
            for (int i_4_ = 0; i_4_ < 8; i_4_++) {
                aLongArray1471[i_4_] = 0L;
                int i_5_ = 0;
                int i_6_ = 56;
                for (/**/; i_5_ < 8; i_5_++) {
                    aLongArray1471[i_4_] = (Class105_Sub2.method993(aLongArray1471[i_4_], (InputStream_Sub1.aLongArrayArray75[i_5_][(Class139.method1166(255, (int) ((aLongArray1472[Class139.method1166(7, i_4_ - i_5_)]) >>> i_6_)))])));
                    i_6_ -= 8;
                }
            }
            for (int i_7_ = 0; i_7_ < 8; i_7_++)
                aLongArray1472[i_7_] = aLongArray1471[i_7_];
            aLongArray1472[0] = Class105_Sub2.method993(aLongArray1472[0], InputStream_Sub1.aLongArray76[i_3_]);
            for (int i_8_ = 0; i_8_ < 8; i_8_++) {
                aLongArray1471[i_8_] = aLongArray1472[i_8_];
                int i_9_ = 0;
                int i_10_ = 56;
                for (/**/; i_9_ < 8; i_9_++) {
                    aLongArray1471[i_8_] = (Class105_Sub2.method993(aLongArray1471[i_8_], (InputStream_Sub1.aLongArrayArray75[i_9_][(Class139.method1166(255, (int) ((aLongArray1469[Class139.method1166(-i_9_ + i_8_, 7)]) >>> i_10_)))])));
                    i_10_ -= 8;
                }
            }
            for (int i_11_ = 0; i_11_ < 8; i_11_++)
                aLongArray1469[i_11_] = aLongArray1471[i_11_];
        }
        for (int i_12_ = 0; i_12_ < 8; i_12_++)
            aLongArray1474[i_12_] = (Class105_Sub2.method993(aLongArray1474[i_12_], Class105_Sub2.method993(aLongArray1473[i_12_], aLongArray1469[i_12_])));
    }

    final void method829(int i) {
        for (int i_19_ = 0; i_19_ < 32; i_19_++)
            aByteArray1465[i_19_] = (byte) 0;
        aByteArray1475[0] = (byte) 0;
        anInt1463 = anInt1468 = 0;
        for (int i_20_ = 0; i_20_ < 8; i_20_++)
            aLongArray1474[i_20_] = 0L;
    }

    final void method832(long l, byte[] is, int i) {
        int i_29_ = 0;
        int i_30_ = 8 - ((int) l & 0x7) & 0x7;
        int i_31_ = anInt1463 & 0x7;
        long l_32_ = l;
        int i_34_ = 0;
        for (int i_33_ = 31; i_33_ >= 0; i_33_--) {
            i_34_ += (0xff & aByteArray1465[i_33_]) - -(0xff & (int) l_32_);
            aByteArray1465[i_33_] = (byte) i_34_;
            l_32_ >>>= 8;
            i_34_ >>>= 8;
        }
        while (l > 8L) {
            int i_36_ = (is[i_29_] << i_30_ & 0xff | (0xff & is[1 + i_29_]) >>> 8 + -i_30_);
            aByteArray1475[anInt1468] = (byte) Class273.or(aByteArray1475[anInt1468], i_36_ >>> i_31_);
            anInt1468++;
            anInt1463 += 8 - i_31_;
            if (anInt1463 == 512) {
                method827(0);
                anInt1463 = anInt1468 = 0;
            }
            aByteArray1475[anInt1468] = (byte) Class139.method1166(i_36_ << -i_31_ + 8, 255);
            i_29_++;
            l -= 8L;
            anInt1463 += i_31_;
        }
        int i_37_;
        if (l > 0L) {
            i_37_ = 0xff & is[i_29_] << i_30_;
            aByteArray1475[anInt1468] = (byte) Class273.or(aByteArray1475[anInt1468], i_37_ >>> i_31_);
        } else i_37_ = 0;
        if (l + (long) i_31_ >= 8) {
            anInt1463 += 8 - i_31_;
            l -= -i_31_ + 8;
            anInt1468++;
            if (anInt1463 == 512) {
                method827(0);
                anInt1463 = anInt1468 = 0;
            }
            aByteArray1475[anInt1468] = (byte) Class139.method1166(i_37_ << 8 + -i_31_, 255);
            anInt1463 += (int) l;
        } else anInt1463 += l;
    }

    final void method833(boolean bool, int i, byte[] is) {
        aByteArray1475[anInt1468] = (byte) Class273.or(aByteArray1475[anInt1468], 128 >>> Class139.method1166(anInt1463, 7));
        anInt1468++;
        if (anInt1468 > 32) {
            while (anInt1468 < 64) aByteArray1475[anInt1468++] = (byte) 0;
            method827(0);
            anInt1468 = 0;
        }
        while (anInt1468 < 32) aByteArray1475[anInt1468++] = (byte) 0;
        Class214.method1577(aByteArray1465, 0, aByteArray1475, 32, 32);
        method827(0);
        int i_38_ = 0;
        int i_39_ = i;
        while (i_38_ < 8) {
            long l = aLongArray1474[i_38_];
            is[i_39_] = (byte) (int) (l >>> 56);
            is[i_39_ + 1] = (byte) (int) (l >>> 48);
            is[2 + i_39_] = (byte) (int) (l >>> 40);
            is[i_39_ - -3] = (byte) (int) (l >>> 32);
            is[i_39_ + 4] = (byte) (int) (l >>> 24);
            is[i_39_ + 5] = (byte) (int) (l >>> 16);
            is[6 + i_39_] = (byte) (int) (l >>> 8);
            is[i_39_ + 7] = (byte) (int) l;
            i_38_++;
            i_39_ += 8;
        }
    }

    public Class85() {
        anInt1468 = 0;
        anInt1463 = 0;
        aLongArray1472 = new long[8];
        aLongArray1473 = new long[8];
        aByteArray1475 = new byte[64];
        aLongArray1469 = new long[8];
        aLongArray1474 = new long[8];
    }
}
