package world.gregs.voidps.tools.icon;/* Class214 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class214 {
    static final void method1577(byte[] is, int i, byte[] is_10_, int i_11_, int i_12_) {
        if (is == is_10_) {
            if (i == i_11_) return;
            if (i_11_ > i && i_11_ < i + i_12_) {
                i_12_--;
                i += i_12_;
                i_11_ += i_12_;
                i_12_ = i - i_12_;
                i_12_ += 7;
                while (i >= i_12_) {
                    is_10_[i_11_--] = is[i--];
                    is_10_[i_11_--] = is[i--];
                    is_10_[i_11_--] = is[i--];
                    is_10_[i_11_--] = is[i--];
                    is_10_[i_11_--] = is[i--];
                    is_10_[i_11_--] = is[i--];
                    is_10_[i_11_--] = is[i--];
                    is_10_[i_11_--] = is[i--];
                }
                i_12_ -= 7;
                while (i >= i_12_) is_10_[i_11_--] = is[i--];
                return;
            }
        }
        i_12_ += i;
        i_12_ -= 7;
        while (i < i_12_) {
            is_10_[i_11_++] = is[i++];
            is_10_[i_11_++] = is[i++];
            is_10_[i_11_++] = is[i++];
            is_10_[i_11_++] = is[i++];
            is_10_[i_11_++] = is[i++];
            is_10_[i_11_++] = is[i++];
            is_10_[i_11_++] = is[i++];
            is_10_[i_11_++] = is[i++];
        }
        i_12_ += 7;
        while (i < i_12_) is_10_[i_11_++] = is[i++];
    }

    static final void method1579(int[] is, int i, int i_16_, int i_17_) {
        i_16_ = i + i_16_ - 7;
        while (i < i_16_) {
            is[i++] = i_17_;
            is[i++] = i_17_;
            is[i++] = i_17_;
            is[i++] = i_17_;
            is[i++] = i_17_;
            is[i++] = i_17_;
            is[i++] = i_17_;
            is[i++] = i_17_;
        }
        i_16_ += 7;
        while (i < i_16_) is[i++] = i_17_;
    }
}
