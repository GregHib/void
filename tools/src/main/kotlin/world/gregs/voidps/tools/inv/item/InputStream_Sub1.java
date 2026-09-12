package world.gregs.voidps.tools.inv.item;/* InputStream_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 *
 * Trimmed: only the Whirlpool-style S-box/round-constant tables used by
 * Class85 (SHA/Whirlpool-like digest, needed for Class291's constructor
 * checksum verification) are kept - the network/rendering methods on the
 * original InputStream subclass are unrelated to icon rendering.
 */

final class InputStream_Sub1 {
    static long[][] aLongArrayArray75;
    static long[] aLongArray76 = new long[11];

    static {
        aLongArrayArray75 = new long[8][256];
        for (int i = 0; i < 256; i++) {
            int i_8_ = "\u1823\uc6e8\u87b8\u014f\u36a6\ud2f5\u796f\u9152\u60bc\u9b8e\ua30c\u7b35\u1de0\ud7c2\u2e4b\ufe57\u1577\u37e5\u9ff0\u4ada\u58c9\u290a\ub1a0\u6b85\ubd5d\u10f4\ucb3e\u0567\ue427\u418b\ua77d\u95d8\ufbee\u7c66\udd17\u479e\uca2d\ubf07\uad5a\u8333\u6302\uaa71\uc819\u49d9\uf2e3\u5b88\u9a26\u32b0\ue90f\ud580\ubecd\u3448\uff7a\u905f\u2068\u1aae\ub454\u9322\u64f1\u7312\u4008\uc3ec\udba1\u8d3d\u9700\ucf2b\u7682\ud61b\ub5af\u6a50\u45f3\u30ef\u3f55\ua2ea\u65ba\u2fc0\ude1c\ufd4d\u9275\u068a\ub2e6\u0e1f\u62d4\ua896\uf9c5\u2559\u8472\u394c\u5e78\u388c\ud1a5\ue261\ub321\u9c1e\u43c7\ufc04\u5199\u6d0d\ufadf\u7e24\u3bab\uce11\u8f4e\ub7eb\u3c81\u94f7\ub913\u2cd3\ue76e\uc403\u5644\u7fa9\u2abb\uc153\udc0b\u9d6c\u3174\uf646\uac89\u14e1\u163a\u6909\u70b6\ud0ed\ucc42\u98a4\u285c\uf886".charAt(i / 2);
            long l = (i & 0x1) == 0 ? i_8_ >>> 8 : 0xff & i_8_;
            long l_9_ = l << 1;
            if (l_9_ >= 256) l_9_ ^= 0x11dL;
            long l_10_ = l_9_ << 1;
            if (l_10_ >= 256L) l_10_ ^= 0x11dL;
            long l_11_ = l ^ l_10_;
            long l_12_ = l_10_ << 1;
            if (l_12_ >= 256L) l_12_ ^= 0x11dL;
            long l_13_ = l_12_ ^ l;
            aLongArrayArray75[0][i] = (Class277.method2068(l_13_, (Class277.method2068(l_9_ << 8, (Class277.method2068(l_11_ << 16, (Class277.method2068(l_12_ << 24, (Class277.method2068(l << 32, (Class277.method2068(Class277.method2068(l << 56, l << 48), l_10_ << 40))))))))))));
            for (int i_14_ = 1; i_14_ < 8; i_14_++)
                aLongArrayArray75[i_14_][i] = Class277.method2068((aLongArrayArray75[i_14_ + -1][i] >>> 8), (aLongArrayArray75[i_14_ - 1][i] << 56));
        }
        aLongArray76[0] = 0L;
        for (int i = 1; i <= 10; i++) {
            int i_15_ = -8 + i * 8;
            aLongArray76[i] = (Class105_Sub2.method993((Class105_Sub2.method993(Class348_Sub8.method2777(aLongArrayArray75[6][6 + i_15_], 65280L), (Class105_Sub2.method993(Class348_Sub8.method2777((aLongArrayArray75[5][5 + i_15_]), 16711680L), (Class105_Sub2.method993((Class105_Sub2.method993((Class105_Sub2.method993(Class348_Sub8.method2777((aLongArrayArray75[2][i_15_ + 2]), 280375465082880L), (Class105_Sub2.method993(Class348_Sub8.method2777(71776119061217280L, (aLongArrayArray75[1][1 + i_15_])), (Class348_Sub8.method2777(aLongArrayArray75[0][i_15_], -72057594037927936L)))))), Class348_Sub8.method2777(1095216660480L, (aLongArrayArray75[3][3 + i_15_])))), Class348_Sub8.method2777((aLongArrayArray75[4][i_15_ - -4]), 4278190080L))))))), Class348_Sub8.method2777(aLongArrayArray75[7][7 + i_15_], 255L)));
        }
    }
}
