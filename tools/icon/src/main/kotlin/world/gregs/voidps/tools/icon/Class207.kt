package world.gregs.voidps.tools.icon

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index

/* Class207 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Class207 {
    var aByteArray2695: ByteArray? = null
    var anInt2696: Int = 0
    var anIntArray2697: IntArray? = null
    var anInt2698: Int = 0
    var aByteArray2699: ByteArray? = null
    var anInt2700: Int = 0
    var anInt2701: Int = 0
    var anInt2702: Int = 0
    var anInt2703: Int = 0

    fun method1510(): Int {
        return (this.anInt2702 + this.anInt2703 + this.anInt2698)
    }

    fun method1516(): IntArray {
        val i = method1510()
        val `is` = IntArray(i * method1522())
        if (this.aByteArray2695 == null) {
            for (i_67_ in 0..<this.anInt2696) {
                var i_68_ = i_67_ * this.anInt2702
                var i_69_ = (this.anInt2703 + (i_67_ + this.anInt2700) * i)
                for (i_70_ in 0..<this.anInt2702) {
                    val i_71_ = (this.anIntArray2697!![this.aByteArray2699!![i_68_++].toInt() and 0xff])
                    if (i_71_ != 0) `is`[i_69_++] = 0xffffff.inv() or i_71_
                    else `is`[i_69_++] = 0
                }
            }
        } else {
            for (i_63_ in 0..<this.anInt2696) {
                var i_64_ = i_63_ * this.anInt2702
                var i_65_ = (this.anInt2703 + (i_63_ + this.anInt2700) * i)
                for (i_66_ in 0..<this.anInt2702) {
                    `is`[i_65_++] = (this.aByteArray2695!![i_64_].toInt() shl 24 or (this.anIntArray2697!![(this.aByteArray2699!![i_64_].toInt() and 0xff)]))
                    i_64_++
                }
            }
        }
        return `is`
    }

    fun method1522(): Int {
        return (this.anInt2696 + this.anInt2700 + this.anInt2701)
    }

    fun method1524() {
        val i = method1510()
        val i_113_ = method1522()
        if (this.anInt2702 != i || this.anInt2696 != i_113_) {
            val `is` = ByteArray(i * i_113_)
            if (this.aByteArray2695 == null) {
                for (i_119_ in 0..<this.anInt2696) {
                    var i_120_ = i_119_ * this.anInt2702
                    var i_121_ = ((i_119_ + this.anInt2700) * i + this.anInt2703)
                    for (i_122_ in 0..<this.anInt2702) `is`[i_121_++] = this.aByteArray2699!![i_120_++]
                }
            } else {
                val is_114_ = ByteArray(i * i_113_)
                for (i_115_ in 0..<this.anInt2696) {
                    var i_116_ = i_115_ * this.anInt2702
                    var i_117_ = ((i_115_ + this.anInt2700) * i + this.anInt2703)
                    for (i_118_ in 0..<this.anInt2702) {
                        `is`[i_117_] = this.aByteArray2699!![i_116_]
                        is_114_[i_117_++] = this.aByteArray2695!![i_116_++]
                    }
                }
                this.aByteArray2695 = is_114_
            }
            this.anInt2701 = 0
            this.anInt2700 = this.anInt2701
            this.anInt2698 = this.anInt2700
            this.anInt2703 = this.anInt2698
            this.anInt2702 = i
            this.anInt2696 = i_113_
            this.aByteArray2699 = `is`
        }
    }

    companion object {
        fun method1512(cache: Cache, i: Int): Class207? {
            val `is` = cache.data(Index.SPRITES, i)
            if (`is` == null) return null
            return method1517(`is`)[0]
        }

        fun method1521(cache: Cache, i: Int, i_112_: Int): Class207? {
            val `is` = cache.data(Index.SPRITES, i, i_112_)
            if (`is` == null) return null
            return method1517(`is`)[0]
        }

        private fun method1517(`is`: ByteArray): Array<Class207> {
            val packet = Packet(`is`)
            packet.pos = `is`.size - 2
            val i = packet.readUnsignedShort(842397944)
            val class207s: Array<Class207> = Array(i) { Class207() }
            packet.pos = `is`.size - 7 - i * 8
            val i_73_ = packet.readUnsignedShort(842397944)
            val i_74_ = packet.readUnsignedShort(842397944)
            val i_75_ = (packet.readUnsignedByte(255) and 0xff) + 1
            for (i_76_ in 0..<i) class207s[i_76_].anInt2703 = packet.readUnsignedShort(842397944)
            for (i_77_ in 0..<i) class207s[i_77_].anInt2700 = packet.readUnsignedShort(842397944)
            for (i_78_ in 0..<i) class207s[i_78_].anInt2702 = packet.readUnsignedShort(842397944)
            for (i_79_ in 0..<i) class207s[i_79_].anInt2696 = packet.readUnsignedShort(842397944)
            for (i_80_ in 0..<i) {
                val class207 = class207s[i_80_]
                class207.anInt2698 = (i_73_ - class207.anInt2702 - class207.anInt2703)
                class207.anInt2701 = (i_74_ - class207.anInt2696 - class207.anInt2700)
            }
            packet.pos = `is`.size - 7 - i * 8 - (i_75_ - 1) * 3
            val is_81_ = IntArray(i_75_)
            for (i_82_ in 1..<i_75_) {
                is_81_[i_82_] = packet.readMedium(-1)
                if (is_81_[i_82_] == 0) is_81_[i_82_] = 1
            }
            for (i_83_ in 0..<i) class207s[i_83_].anIntArray2697 = is_81_
            packet.pos = 0
            for (i_84_ in 0..<i) {
                val class207 = class207s[i_84_]
                val i_85_ = (class207.anInt2702 * class207.anInt2696)
                class207.aByteArray2699 = ByteArray(i_85_)
                val i_86_ = packet.readUnsignedByte(255)
                if ((i_86_ and 0x2) == 0) {
                    if ((i_86_ and 0x1) == 0) {
                        for (i_87_ in 0..<i_85_) class207.aByteArray2699!![i_87_] = packet.readByte(-126)
                    } else {
                        for (i_88_ in 0..<class207.anInt2702) {
                            for (i_89_ in 0..<class207.anInt2696) class207.aByteArray2699!![(i_88_ + i_89_ * class207.anInt2702)] = packet.readByte(-96)
                        }
                    }
                } else {
                    var bool = false
                    class207.aByteArray2695 = ByteArray(i_85_)
                    if ((i_86_ and 0x1) == 0) {
                        for (i_90_ in 0..<i_85_) class207.aByteArray2699!![i_90_] = packet.readByte(-118)
                        for (i_91_ in 0..<i_85_) {
                            val i_92_ = (packet.readByte(-89).also { class207.aByteArray2695!![i_91_] = it })
                            bool = bool or (i_92_.toInt() != -1)
                        }
                    } else {
                        for (i_93_ in 0..<class207.anInt2702) {
                            for (i_94_ in 0..<class207.anInt2696) class207.aByteArray2699!![(i_93_ + i_94_ * class207.anInt2702)] = packet.readByte(-84)
                        }
                        for (i_95_ in 0..<class207.anInt2702) {
                            for (i_96_ in 0..<class207.anInt2696) {
                                val i_97_ = (packet.readByte(-122).also { class207.aByteArray2695!![i_95_ + i_96_ * (class207.anInt2702)] = it })
                                bool = bool or (i_97_.toInt() != -1)
                            }
                        }
                    }
                    if (!bool) class207.aByteArray2695 = null
                }
            }
            return class207s
        }
    }
}
