package world.gregs.voidps.tools.icon

/* Class214 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal object Class214 {
    fun method1577(`is`: ByteArray, i: Int, is_10_: ByteArray, i_11_: Int, i_12_: Int) {
        var i = i
        var i_11_ = i_11_
        var i_12_ = i_12_
        if (`is` == is_10_) {
            if (i == i_11_) return
            if (i_11_ > i && i_11_ < i + i_12_) {
                i_12_--
                i += i_12_
                i_11_ += i_12_
                i_12_ = i - i_12_
                i_12_ += 7
                while (i >= i_12_) {
                    is_10_[i_11_--] = `is`[i--]
                    is_10_[i_11_--] = `is`[i--]
                    is_10_[i_11_--] = `is`[i--]
                    is_10_[i_11_--] = `is`[i--]
                    is_10_[i_11_--] = `is`[i--]
                    is_10_[i_11_--] = `is`[i--]
                    is_10_[i_11_--] = `is`[i--]
                    is_10_[i_11_--] = `is`[i--]
                }
                i_12_ -= 7
                while (i >= i_12_) is_10_[i_11_--] = `is`[i--]
                return
            }
        }
        i_12_ += i
        i_12_ -= 7
        while (i < i_12_) {
            is_10_[i_11_++] = `is`[i++]
            is_10_[i_11_++] = `is`[i++]
            is_10_[i_11_++] = `is`[i++]
            is_10_[i_11_++] = `is`[i++]
            is_10_[i_11_++] = `is`[i++]
            is_10_[i_11_++] = `is`[i++]
            is_10_[i_11_++] = `is`[i++]
            is_10_[i_11_++] = `is`[i++]
        }
        i_12_ += 7
        while (i < i_12_) is_10_[i_11_++] = `is`[i++]
    }

    fun method1579(`is`: IntArray, i: Int, i_16_: Int, i_17_: Int) {
        var i = i
        var i_16_ = i_16_
        i_16_ = i + i_16_ - 7
        while (i < i_16_) {
            `is`[i++] = i_17_
            `is`[i++] = i_17_
            `is`[i++] = i_17_
            `is`[i++] = i_17_
            `is`[i++] = i_17_
            `is`[i++] = i_17_
            `is`[i++] = i_17_
            `is`[i++] = i_17_
        }
        i_16_ += 7
        while (i < i_16_) `is`[i++] = i_17_
    }
}
