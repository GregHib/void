package world.gregs.voidps.tools.icon

/* Class348_Sub49 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal open class Packet : Class348 {
    var aByteArray7154: ByteArray
    var pos: Int

    fun method3367(i: Int, `is`: IntArray, i_47_: Int, i_48_: Int) {
        anInt7178++
        val i_49_ = this.pos
        this.pos = i_47_
        val i_50_ = (i_48_ + -i_47_) / 8
        var i_51_ = 0
        while (i_50_ > i_51_) {
            var i_52_ = readInt((-126).toByte())
            var i_53_ = readInt((-126).toByte())
            var i_54_ = -957401312
            val i_55_ = -1640531527
            var i_56_ = 32
            while (i_56_-- > 0) {
                i_53_ -= ((i_52_ shl 4 xor (i_52_ ushr 5)) + i_52_ xor i_54_ - -`is`[0x4d000003 and (i_54_ ushr 11)])
                i_54_ -= i_55_
                i_52_ -= (i_54_ - -`is`[i_54_ and 0x3] xor (i_53_ shl 4 xor (i_53_ ushr 5)) - -i_53_)
            }
            this.pos -= 8
            writeInt(113.toByte(), i_52_)
            writeInt(126.toByte(), i_53_)
            i_51_++
        }
        if (i == 607818341) this.pos = i_49_
    }

    fun readUnsignedShort(i: Int): Int {
        if (i != 842397944) return 111
        this.pos += 2
        anInt7186++
        return ((0xff and (this.aByteArray7154[-1 + this.pos]).toInt()) + ((this.aByteArray7154[-2 + this.pos]).toInt() shl 8 and 0xff00))
    }

    fun method3362(i: Byte): Int {
        anInt7155++
        val i_43_ = ((this.aByteArray7154[this.pos]).toInt() and 0xff)
        if (i.toInt() != 77) readByte(-48)
        if (i_43_ < 128) return -64 + readUnsignedByte(255)
        return readUnsignedShort(i.toInt() xor 0x3235f8b5) - 49152
    }

    fun readMedium(i: Int): Int {
        this.pos += 3
        anInt7203++
        if (i != -1) return -52
        return ((0xff00 and ((this.aByteArray7154[-2 + this.pos]).toInt() shl 8)) + ((((this.aByteArray7154[-3 + this.pos]).toInt() and 0xff) shl 16) - -((this.aByteArray7154[-1 + this.pos]).toInt() and 0xff)))
    }

    fun readShort(i: Int): Int {
        anInt7204++
        if (i != 13638) method3350(-23, true, null, -10)
        this.pos += 2
        var i_65_ = (((this.aByteArray7154[this.pos - 1]).toInt() and 0xff) + (((this.aByteArray7154[-2 + this.pos]).toInt() and 0xff) shl 8))
        if (i_65_ > 32767) i_65_ -= 65536
        return i_65_
    }

    fun readString(i: Byte): String {
        anInt7166++
        val i_68_ = -81 / ((i - 30) / 52)
        val i_69_ = this.pos
        while ((this.aByteArray7154[this.pos++]).toInt() != 0) {
            /* empty */
        }
        val i_70_ = -1 + this.pos - i_69_
        if (i_70_ == 0) return ""
        return Class367_Sub8.method3546(this.aByteArray7154, 0, i_70_, i_69_)
    }

    fun readUnsignedByte(i: Int): Int {
        if (i != 255) writeBytes(-101, 111, null, 33)
        anInt7153++
        return ((this.aByteArray7154[this.pos++]).toInt() and 0xff)
    }

    fun readByte(i: Int): Byte {
        if (i >= -75) writeByteAdd((-18).toByte(), -24)
        anInt7143++
        return (this.aByteArray7154[this.pos++])
    }

    fun gdata(i: Int, i_82_: Int, i_83_: Int, `is`: ByteArray) {
        anInt7159++
        var i_84_ = i_82_
        while (i_83_ + i_82_ > i_84_) {
            `is`[i_84_] = (this.aByteArray7154[this.pos++])
            i_84_++
        }
        if (i != 2147483647) anInt7207 = -47
    }

    constructor(i: Int) {
        this.pos = 0
        this.aByteArray7154 = Class37.method359(i, -1)!!
    }

    constructor(`is`: ByteArray) {
        this.aByteArray7154 = `is`
        this.pos = 0
    }

    fun readInt(i: Byte): Int {
        anInt7196++
        this.pos += 4
        if (i.toInt() != -126) method3368(-61, -64)
        return ((0xff and (this.aByteArray7154[this.pos - 1]).toInt()) + ((((this.aByteArray7154[-4 + this.pos]).toInt() and 0xff) shl 24) + (0xff0000 and ((this.aByteArray7154[-3 + this.pos]).toInt() shl 16))) - -(((this.aByteArray7154[-2 + this.pos]).toInt() and 0xff) shl 8))
    }

    fun method3350(i: Int, bool: Boolean, `is`: IntArray?, i_25_: Int) {
        anInt7137++
        val i_26_ = this.pos
        this.pos = i
        val i_27_ = (-i + i_25_) / 8
        var i_28_ = 0
        while (i_27_ > i_28_) {
            var i_29_ = readInt((-126).toByte())
            var i_30_ = readInt((-126).toByte())
            var i_31_ = 0
            val i_32_ = -1640531527
            var i_33_ = 32
            while (i_33_-- > 0) {
                i_29_ += (i_31_ - -`is`!![i_31_ and 0x3] xor (i_30_ ushr 5 xor (i_30_ shl 4)) - -i_30_)
                i_31_ += i_32_
                i_30_ += (i_31_ - -`is`[(0x1a0b and i_31_) ushr 11] xor i_29_ + (i_29_ ushr 5 xor (i_29_ shl 4)))
            }
            this.pos -= 8
            writeInt(91.toByte(), i_29_)
            writeInt(98.toByte(), i_30_)
            i_28_++
        }
        if (bool != true) method3394(88, 83)
        this.pos = i_26_
    }

    fun method3368(i: Int, i_57_: Int): Long {
        var i = i
        i--
        anInt7191++
        require(!(i < 0 || i > 7))
        if (i_57_ != 3060) return 99L
        var i_58_ = 8 * i
        var l = 0L
        while ( /**/i_58_ >= 0) {
            l = l or (((this.aByteArray7154[this.pos++]).toLong() and 0xffL) shl i_58_)
            i_58_ -= 8
        }
        return l
    }

    fun writeBytes(i: Int, i_73_: Int, `is`: ByteArray?, i_74_: Int) {
        var i_75_ = i_73_
        while (i_73_ + i > i_75_) {
            this.aByteArray7154[this.pos++] = `is`!![i_75_]
            i_75_++
        }
        val i_76_ = -41 % ((8 - i_74_) / 52)
        anInt7199++
    }

    fun writeByteAdd(i: Byte, i_94_: Int) {
        anInt7192++
        this.aByteArray7154[this.pos++] = (i_94_ + 128).toByte()
        val i_95_ = -21 % ((-8 - i) / 57)
    }

    fun writeInt(i: Byte, i_90_: Int) {
        this.aByteArray7154[this.pos++] = (i_90_ shr 24).toByte()
        if (i < 84) writeByteAdd((-122).toByte(), -112)
        anInt7202++
        this.aByteArray7154[this.pos++] = (i_90_ shr 16).toByte()
        this.aByteArray7154[this.pos++] = (i_90_ shr 8).toByte()
        this.aByteArray7154[this.pos++] = i_90_.toByte()
    }

    fun method3394(i: Int, i_93_: Int) {
        this.aByteArray7154[this.pos++] = i_93_.toByte()
        anInt7141++
        this.aByteArray7154[this.pos++] = (i_93_ shr 8).toByte()
        if (i == -23892) {
            this.aByteArray7154[this.pos++] = (i_93_ shr 16).toByte()
            this.aByteArray7154[this.pos++] = (i_93_ shr 24).toByte()
        }
    }

    companion object {
        var anInt7137: Int = 0
        var anInt7141: Int = 0
        var anInt7143: Int = 0
        var anInt7144: Int = 0
        var anInt7150: Int = 0
        var anInt7153: Int = 0
        var anInt7155: Int = 0
        var anInt7158: Int = 0
        var anInt7159: Int = 0
        var anInt7166: Int = 0
        var anInt7186: Int = 0
        var anInt7191: Int = 0
        var anInt7192: Int = 0
        var anInt7196: Int = 0
        var anInt7199: Int = 0
        var anInt7202: Int = 0
        var anInt7203: Int = 0
        var anInt7204: Int = 0
        var anInt7178: Int = 0
        var anInt7207: Int = 0
    }
}
