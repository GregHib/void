package world.gregs.voidps.tools.render

/* Class348_Sub49 */

open class Packet(var aByteArray7154: ByteArray) : Node() {
    var pos: Int = 0

    fun readUnsignedShort(): Int {
        this.pos += 2
        return ((0xff and (this.aByteArray7154[-1 + this.pos]).toInt()) + ((this.aByteArray7154[-2 + this.pos]).toInt() shl 8 and 0xff00))
    }

    fun method3362(): Int {
        val i_43_ = ((this.aByteArray7154[this.pos]).toInt() and 0xff)
        if (i_43_ < 128) return -64 + readUnsignedByte()
        return readUnsignedShort() - 49152
    }

    fun readMedium(): Int {
        this.pos += 3
        return ((0xff00 and ((this.aByteArray7154[-2 + this.pos]).toInt() shl 8)) + ((((this.aByteArray7154[-3 + this.pos]).toInt() and 0xff) shl 16) - -((this.aByteArray7154[-1 + this.pos]).toInt() and 0xff)))
    }

    fun readShort(): Int {
        this.pos += 2
        var i_65_ = (((this.aByteArray7154[this.pos - 1]).toInt() and 0xff) + (((this.aByteArray7154[-2 + this.pos]).toInt() and 0xff) shl 8))
        if (i_65_ > 32767) i_65_ -= 65536
        return i_65_
    }

    fun readString(): String {
        val i_69_ = this.pos
        while ((this.aByteArray7154[this.pos++]).toInt() != 0) {
        }
        val i_70_ = -1 + this.pos - i_69_
        if (i_70_ == 0) return ""
        return method3546(this.aByteArray7154, i_70_, i_69_)
    }

    fun readUnsignedByte(): Int {
        return ((this.aByteArray7154[this.pos++]).toInt() and 0xff)
    }

    fun readByte(): Byte {
        return (this.aByteArray7154[this.pos++])
    }

    fun readInt(): Int {
        this.pos += 4
        return ((0xff and (this.aByteArray7154[this.pos - 1]).toInt()) + ((((this.aByteArray7154[-4 + this.pos]).toInt() and 0xff) shl 24) + (0xff0000 and ((this.aByteArray7154[-3 + this.pos]).toInt() shl 16))) - -(((this.aByteArray7154[-2 + this.pos]).toInt() and 0xff) shl 8))
    }

    companion object {
        var aCharArray625: CharArray = charArrayOf('€', '\u0000', '‚', 'ƒ', '„', '…', '†', '‡', 'ˆ', '‰', 'Š', '‹', 'Œ', '\u0000', 'Ž', '\u0000', '\u0000', '‘', '’', '“', '”', '•', '–', '—', '˜', '™', 'š', '›', 'œ', '\u0000', 'ž', 'Ÿ')

        fun method3546(`is`: ByteArray, i_0_: Int, i_1_: Int): String {
            val cs = CharArray(i_0_)
            var i_2_ = 0
            for (i_3_ in 0..<i_0_) {
                var i_4_ = 0xff and `is`[i_3_ + i_1_].toInt()
                if (i_4_ != 0) {
                    if (i_4_ >= 128 && i_4_ < 160) {
                        var i_5_ = aCharArray625[i_4_ - 128].code
                        if (i_5_ == 0) i_5_ = 63
                        i_4_ = i_5_
                    }
                    cs[i_2_++] = i_4_.toChar()
                }
            }
            return String(cs, 0, i_2_)
        }
    }
}
