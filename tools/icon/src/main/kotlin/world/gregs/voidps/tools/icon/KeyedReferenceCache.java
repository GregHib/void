package world.gregs.voidps.tools.icon;/* Class175 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class KeyedReferenceCache {
    private int anInt2311;
    private final IterableHashTable table;
    static int anInt2313;
    private Queue history = new Queue();
    private final int anInt2324;
    static int anInt2323;

    final Object get(int i, CacheKey cacheKey) {
        anInt2313++;
        long hash = cacheKey.toLong((byte) 64);
        for (KeyReferenceNode node = (KeyReferenceNode) table.method3480(hash, -6008); node != null; node = (KeyReferenceNode) table.method3476(true)) {
            if (node.cacheKey.matches(94, cacheKey)) {
                Object object = node.get(65536);
                if (object == null) {
                    node.unlink((byte) 36);
                    node.unlink2(true);
                    anInt2311 += (node.anInt9556);
                } else {
                    if (node.method3206((byte) -128)) {
                        KeyedHardReferenceNode hardReference = (new KeyedHardReferenceNode(cacheKey, object, (node.anInt9556)));
                        table.put((byte) 125, (node.aLong4291), hardReference);
                        history.add(true, hardReference);
                        hardReference.key2 = 0L;
                        node.unlink((byte) 65);
                        node.unlink2(true);
                    } else {
                        history.add(true, node);
                        node.key2 = 0L;
                    }
                    return object;
                }
            }
        }
        if (i < 66) return null;
        return null;
    }

    static final float[] method1347(int i, int i_6_, float f, float f_7_, int i_8_, float f_9_, int i_10_, int i_11_) {
        anInt2323++;
        float[] fs = new float[9];
        float[] fs_12_ = new float[9];
        float f_13_ = (float) Math.cos((float) i_11_ * 0.024543693F);
        int i_14_ = -94 / ((i_8_ - 57) / 62);
        float f_15_ = (float) Math.sin(0.024543693F * (float) i_11_);
        fs[6] = -f_15_;
        float f_16_ = -f_13_ + 1.0F;
        fs[8] = f_13_;
        fs[3] = 0.0F;
        fs[1] = 0.0F;
        fs[2] = f_15_;
        fs[4] = 1.0F;
        fs[5] = 0.0F;
        fs[0] = f_13_;
        fs[7] = 0.0F;
        float[] fs_17_ = new float[9];
        float f_18_ = 1.0F;
        f_13_ = (float) i_6_ / 32767.0F;
        float f_19_ = 0.0F;
        f_16_ = -f_13_ + 1.0F;
        f_15_ = -(float) Math.sqrt(1.0F - f_13_ * f_13_);
        float f_20_ = (float) Math.sqrt(i_10_ * i_10_ + i * i);
        if (f_20_ == 0.0F && f_13_ == 0.0F) fs_12_ = fs;
        else {
            if (f_20_ != 0.0F) {
                f_18_ = (float) -i / f_20_;
                f_19_ = (float) i_10_ / f_20_;
            }
            fs_17_[5] = f_18_ * f_15_;
            fs_17_[2] = f_18_ * f_19_ * f_16_;
            fs_17_[8] = f_13_ + f_16_ * (f_19_ * f_19_);
            fs_17_[4] = f_13_;
            fs_17_[0] = f_16_ * (f_18_ * f_18_) + f_13_;
            fs_17_[6] = f_16_ * (f_19_ * f_18_);
            fs_17_[3] = f_15_ * -f_19_;
            fs_17_[1] = f_15_ * f_19_;
            fs_17_[7] = f_15_ * -f_18_;
            fs_12_[0] = fs_17_[0] * fs[0] + fs[1] * fs_17_[3] + fs_17_[6] * fs[2];
            fs_12_[1] = fs_17_[7] * fs[2] + (fs[1] * fs_17_[4] + fs[0] * fs_17_[1]);
            fs_12_[2] = fs[1] * fs_17_[5] + fs[0] * fs_17_[2] + fs[2] * fs_17_[8];
            fs_12_[3] = fs_17_[0] * fs[3] + fs[4] * fs_17_[3] + fs_17_[6] * fs[5];
            fs_12_[4] = fs[5] * fs_17_[7] + (fs[3] * fs_17_[1] + fs[4] * fs_17_[4]);
            fs_12_[6] = fs_17_[0] * fs[6] + fs[7] * fs_17_[3] + fs_17_[6] * fs[8];
            fs_12_[5] = fs[4] * fs_17_[5] + fs_17_[2] * fs[3] + fs[5] * fs_17_[8];
            fs_12_[7] = fs_17_[1] * fs[6] + fs_17_[4] * fs[7] + fs[8] * fs_17_[7];
            fs_12_[8] = fs_17_[5] * fs[7] + fs[6] * fs_17_[2] + fs[8] * fs_17_[8];
        }
        fs_12_[7] *= f;
        fs_12_[4] *= f_9_;
        fs_12_[3] *= f_9_;
        fs_12_[5] *= f_9_;
        fs_12_[2] *= f_7_;
        fs_12_[8] *= f;
        fs_12_[6] *= f;
        fs_12_[1] *= f_7_;
        fs_12_[0] *= f_7_;
        return fs_12_;
    }

    KeyedReferenceCache(int i) {
        anInt2311 = i;
        anInt2324 = i;
        int i_22_;
        for (i_22_ = 1; i_22_ + i_22_ < i; i_22_ += i_22_) {
            /* empty */
        }
        table = new IterableHashTable(i_22_);
    }
}
