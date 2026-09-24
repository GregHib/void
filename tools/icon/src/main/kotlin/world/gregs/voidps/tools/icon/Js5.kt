package world.gregs.voidps.tools.icon

/* Class45 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Js5(resourceProvider: ResourceProvider?, discardPacked: Boolean, discardUnpacked: Int) {
    private var index: Js5Index? = null
    var discardUnpacked: Int
    private val discardPacked: Boolean
    private var packed: Array<Any?>
    private var provider: ResourceProvider?
    private var unpacked: Array<Array<Any?>?>?

    fun getCrc(i: Int): Int {
        val i_0_ = -117 / ((-60 - i) / 33)
        anInt657++
        check(indexReady(false)) { "" }
        return index!!.crc
    }

    private fun isValidGroup(i: Int, i_4_: Byte): Boolean {
        anInt646++
        if (!indexReady(false)) return false
        if (i < 0 || index!!.fileLimits.size <= i || (index!!.fileLimits[i] == 0)) {
            require(!Class285.aBoolean4741) { i.toString() }
            return false
        }
        return i_4_.toInt() == -40
    }

    fun getFile(i: Int, i_5_: Int, i_6_: Int, `is`: IntArray?): ByteArray? {
        anInt639++
        if (i_6_ != 2) anInt669 = 51
        if (!isValidFile(i_5_, i_6_ + -2, i)) return null
        if (unpacked!![i] == null || unpacked!![i]!![i_5_] == null) {
            var bool = unpackFile(i_5_, -78.toByte(), `is`, i)
            if (!bool) {
                fetchGroup(i, -117)
                bool = unpackFile(i_5_, -103.toByte(), `is`, i)
                if (!bool) return null
            }
        }
        val is_7_ = Class50_Sub1.unwrap(false, unpacked!![i]!![i_5_], 53146732)
        if (this.discardUnpacked == 1) {
            unpacked!![i]!![i_5_] = null
            if (index!!.fileLimits[i] == 1) unpacked!![i] = null
        } else if (this.discardUnpacked == 2) unpacked!![i] = null
        return is_7_
    }

    private fun unpackFile(i: Int, i_8_: Byte, `is`: IntArray?, groupId: Int): Boolean {
        anInt628++
        if (!isValidGroup(groupId, -40.toByte())) return false
        if (packed[groupId] == null) return false
        val i_10_ = index!!.fileCounts[groupId]
        val is_11_ = index!!.fileIds[groupId]
        if (unpacked!![groupId] == null) unpacked!![groupId] = arrayOfNulls<Any>(index!!.fileLimits[groupId])
        val objects = unpacked!![groupId]!!
        var bool = true
        var i_12_ = 0
        while (i_10_ > i_12_) {
            val i_13_: Int
            if (is_11_ == null) i_13_ = i_12_
            else i_13_ = is_11_[i_12_]
            if (objects[i_13_] == null) {
                bool = false
                break
            }
            i_12_++
        }
        if (bool) return true
        val unpacked: ByteArray?
        if (`is` == null || (`is`[0] == 0 && `is`[1] == 0 && `is`[2] == 0 && `is`[3] == 0)) {
            unpacked = Class50_Sub1.unwrap(false, packed[groupId], 53146732)
        } else {
            unpacked = Class50_Sub1.unwrap(true, packed[groupId], 53146732)
            val packet = Packet(unpacked)
            packet.method3367(607818341, `is`, 5, (packet.aByteArray7154).size)
        }
        val is_15_: ByteArray?
        try {
            is_15_ = Class348_Sub41.Companion.decodeContainer(unpacked, -120)
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("T3 - " + (`is` != null) + "," + groupId + "," + unpacked.size + "," + Class59_Sub1.method554(5126, unpacked.size, unpacked) + "," + Class59_Sub1.method554(5126, -2 + unpacked.size, unpacked) + "," + index!!.groupCrcs[groupId] + "," + index!!.crc))
        }
        if (discardPacked) packed[groupId] = null
        if (i_8_ >= -17) getFile(70.toByte(), -7)
        if (i_10_ > 1) {
            if (this.discardUnpacked == 2) {
                var i_30_ = is_15_!!.size
                val i_31_ = 0xff and is_15_[--i_30_].toInt()
                i_30_ -= 4 * (i_31_ * i_10_)
                val packet = Packet(is_15_)
                var i_32_ = 0
                var i_33_ = 0
                packet.pos = i_30_
                for (i_34_ in 0..<i_31_) {
                    var i_35_ = 0
                    for (i_36_ in 0..<i_10_) {
                        i_35_ += packet.readInt(-126.toByte())
                        val i_37_: Int
                        if (is_11_ == null) i_37_ = i_36_
                        else i_37_ = is_11_[i_36_]
                        if (i == i_37_) {
                            i_33_ = i_37_
                            i_32_ += i_35_
                        }
                    }
                }
                if (i_32_ == 0) return true
                val is_38_ = ByteArray(i_32_)
                packet.pos = i_30_
                i_32_ = 0
                var i_39_ = 0
                for (i_40_ in 0..<i_31_) {
                    var i_41_ = 0
                    for (i_42_ in 0..<i_10_) {
                        i_41_ += packet.readInt(-126.toByte())
                        val i_43_: Int
                        if (is_11_ != null) i_43_ = is_11_[i_42_]
                        else i_43_ = i_42_
                        if (i_43_ == i) {
                            Class214.method1577(is_15_, i_39_, is_38_, i_32_, i_41_)
                            i_32_ += i_41_
                        }
                        i_39_ += i_41_
                    }
                }
                objects[i_33_] = is_38_
            } else {
                var i_16_ = is_15_!!.size
                val i_17_ = 0xff and is_15_[--i_16_].toInt()
                i_16_ -= 4 * (i_10_ * i_17_)
                val packet = Packet(is_15_)
                val is_18_ = IntArray(i_10_)
                packet.pos = i_16_
                for (i_19_ in 0..<i_17_) {
                    var i_20_ = 0
                    for (i_21_ in 0..<i_10_) {
                        i_20_ += packet.readInt(-126.toByte())
                        is_18_[i_21_] += i_20_
                    }
                }
                val is_22_ = arrayOfNulls<ByteArray>(i_10_)
                var i_23_ = 0
                while (i_10_ > i_23_) {
                    is_22_[i_23_] = ByteArray(is_18_[i_23_])
                    is_18_[i_23_] = 0
                    i_23_++
                }
                packet.pos = i_16_
                var i_24_ = 0
                for (i_25_ in 0..<i_17_) {
                    var i_26_ = 0
                    var i_27_ = 0
                    while (i_10_ > i_27_) {
                        i_26_ += packet.readInt(-126.toByte())
                        Class214.method1577(is_15_, i_24_, is_22_[i_27_], is_18_[i_27_], i_26_)
                        i_24_ += i_26_
                        is_18_[i_27_] += i_26_
                        i_27_++
                    }
                }
                var i_28_ = 0
                while (i_10_ > i_28_) {
                    val i_29_: Int
                    if (is_11_ == null) i_29_ = i_28_
                    else i_29_ = is_11_[i_28_]
                    if (this.discardUnpacked != 0) objects[i_29_] = is_22_[i_28_]
                    else objects[i_29_] = Class179.wrap(is_22_[i_28_], false, 126.toByte())
                    i_28_++
                }
            }
        } else {
            val i_44_: Int
            if (is_11_ != null) i_44_ = is_11_[0]
            else i_44_ = 0
            if (this.discardUnpacked != 0) objects[i_44_] = is_15_
            else objects[i_44_] = Class179.wrap(is_15_, false, 104.toByte())
        }
        return true
    }

    private fun indexReady(bool: Boolean): Boolean {
        anInt652++
        if (index == null) {
            index = provider!!.index(56.toByte())
            if (index == null) return false
            packed = arrayOfNulls<Any>(index!!.groupLimit)
            unpacked = arrayOfNulls<Array<Any?>>(index!!.groupLimit)
        }
        if (bool != false) provider = null
        return true
    }

    private fun fetchGroup(i: Int, i_61_: Int) {
        if (i_61_ > -105) unpacked = null
        if (!discardPacked) packed[i] = Class179.wrap(provider!!.fetchGroup(i, 73.toByte()), false, 123.toByte())
        else packed[i] = provider!!.fetchGroup(i, 12.toByte())
        anInt665++
    }

    fun fileLimit(i: Int, i_62_: Int): Int {
        if (i != 0) getCrc(-61)
        anInt645++
        if (!isValidGroup(i_62_, -40.toByte())) return 0
        return index!!.fileLimits[i_62_]
    }

    fun getFile(i: Int, i_64_: Int, i_65_: Int): ByteArray? {
        if (i != -1860) unpacked = null
        anInt651++
        return getFile(i_64_, i_65_, i xor 0x741.inv(), null)
    }

    fun groupSize(i: Int): Int {
        anInt637++
        if (i != -1) return 49
        if (!indexReady(false)) return -1
        return index!!.fileLimits.size
    }

    fun getFile(i: Byte, i_70_: Int): ByteArray? {
        anInt630++
        if (!indexReady(false)) return null
        if (index!!.fileLimits.size == 1) return getFile(i.toInt() xor 0x70a.inv(), 0, i_70_)
        if (!isValidGroup(i_70_, -40.toByte())) return null
        if (i.toInt() != 73) unpacked = null
        if (index!!.fileLimits[i_70_] == 1) return getFile(i.toInt() xor 0x70a.inv(), i_70_, 0)
        throw RuntimeException()
    }

    private fun isValidFile(i: Int, i_73_: Int, i_74_: Int): Boolean {
        anInt662++
        if (!indexReady(false)) return false
        if (i_74_ < i_73_ || i < 0 || (index!!.fileLimits.size <= i_74_) || (index!!.fileLimits[i_74_] <= i)) {
            require(!Class285.aBoolean4741) { i_74_.toString() + "," + i }
            return false
        }
        return true
    }

    fun requestDownload(i: Int, i_76_: Int, i_77_: Int): Boolean {
        anInt638++
        if (!isValidFile(i_77_, 0, i_76_)) return false
        if (unpacked!![i_76_] != null && unpacked!![i_76_]!![i_77_] != null) return true
        if (i != -10499) return true
        if (packed[i_76_] != null) return true
        fetchGroup(i_76_, -125)
        return packed[i_76_] != null
    }

    fun isFileReady(bool: Boolean, i: Int): Boolean {
        anInt661++
        if (!indexReady(bool)) return false
        if (index!!.fileLimits.size == 1) return requestDownload(-10499, 0, i)
        if (!isValidGroup(i, -40.toByte())) return false
        if (index!!.fileLimits[i] == 1) return requestDownload(-10499, i, 0)
        if (bool != false) return false
        throw RuntimeException()
    }

    init {
        require(!(discardUnpacked < 0 || discardUnpacked > 2)) { "js5: Invalid value " + discardUnpacked + " supplied for discardunpacked" }
        provider = resourceProvider
        this.discardPacked = discardPacked
        this.discardUnpacked = discardUnpacked
    }

    companion object {
        var anInt628: Int = 0
        var anInt630: Int = 0
        var anInt637: Int = 0
        var anInt638: Int = 0
        var anInt639: Int = 0
        var anInt645: Int = 0
        var anInt646: Int = 0
        var anInt647: Int = 0
        var anInt651: Int = 0
        var anInt652: Int = 0
        var anInt657: Int = 0
        var anInt661: Int = 0
        var anInt662: Int = 0
        var anInt665: Int = 0
        var anInt669: Int = 0
    }
}
