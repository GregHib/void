package world.gregs.voidps.tools.icon

/* Class291 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class Js5Index(`is`: ByteArray?, i: Int, is_27_: ByteArray?) {
    var crc: Int = 0
    var fileIds: Array<IntArray?>? = null
    var groupVersions: IntArray? = null
    var groupNameTable: NameHashTable? = null
    var fileLimits: IntArray? = null
    var fileCounts: IntArray? = null
    var groupCount: Int = 0
    var fileNameTables: Array<NameHashTable?>? = null
    var groupCrcs: IntArray? = null
    var groupHashes: Array<ByteArray?>? = null
    var version: Int = 0
    var groupNames: IntArray? = null
    var groupLimit: Int = 0
    var fileNames: Array<IntArray?>? = null
    var groupIds: IntArray? = null
    private var whirlpool: ByteArray? = null
    private fun loadIndex(i: Byte, `is`: ByteArray?) {
        anInt3731++
        val packet: Packet = Packet(Class348_Sub41.Companion.decodeContainer(`is`, -105))
        val i_2_ = packet.readUnsignedByte(255)
        if (i_2_ < 5 || i_2_ > 6) throw RuntimeException()
        if (i_2_ < 6) this.version = 0
        else this.version = packet.readInt((-126).toByte())
        val i_3_ = packet.readUnsignedByte(255)
        val bool = (i_3_ and 0x1) != 0
        val bool_4_ = (i_3_ and 0x2) != 0
        this.groupCount = packet.readUnsignedShort(842397944)
        var i_5_ = 0
        this.groupIds = IntArray(this.groupCount)
        var i_6_ = -1
        for (i_7_ in 0..<this.groupCount) {
            i_5_ += packet.readUnsignedShort(842397944)
            this.groupIds!![i_7_] = i_5_
            if (i_6_ < this.groupIds!![i_7_]) i_6_ = this.groupIds!![i_7_]
        }
        this.groupLimit = i_6_ - -1
        if (bool_4_) this.groupHashes = arrayOfNulls<ByteArray>(this.groupLimit)
        this.groupCrcs = IntArray(this.groupLimit)
        this.fileLimits = IntArray(this.groupLimit)
        this.fileCounts = IntArray(this.groupLimit)
        this.groupVersions = IntArray(this.groupLimit)
        this.fileIds = arrayOfNulls<IntArray>(this.groupLimit)
        if (bool) {
            this.groupNames = IntArray(this.groupLimit)
            for (i_8_ in 0..<this.groupLimit) this.groupNames!![i_8_] = -1
            var i_9_ = 0
            while ((this.groupCount > i_9_)) {
                this.groupNames!![(this.groupIds!![i_9_])] = packet.readInt((-126).toByte())
                i_9_++
            }
            this.groupNameTable = NameHashTable(this.groupNames!!)
        }
        if (i >= -83) loadIndex(42.toByte(), null)
        for (i_10_ in 0..<this.groupCount) this.groupCrcs!![(this.groupIds!![i_10_])] = packet.readInt((-126).toByte())
        if (bool_4_) {
            for (i_11_ in 0..<this.groupCount) {
                val is_12_ = ByteArray(64)
                packet.gdata(2147483647, 0, 64, is_12_)
                this.groupHashes!![(this.groupIds!![i_11_])] = is_12_
            }
        }
        for (i_13_ in 0..<this.groupCount) this.groupVersions!![(this.groupIds!![i_13_])] = packet.readInt((-126).toByte())
        var i_14_ = 0
        while (this.groupCount > i_14_) {
            this.fileCounts!![(this.groupIds!![i_14_])] = packet.readUnsignedShort(842397944)
            i_14_++
        }
        var i_15_ = 0
        while (this.groupCount > i_15_) {
            val i_16_ = this.groupIds!![i_15_]
            i_5_ = 0
            val i_17_ = this.fileCounts!![i_16_]
            var i_18_ = -1
            this.fileIds!![i_16_] = IntArray(i_17_)
            var i_19_ = 0
            while (i_17_ > i_19_) {
                val i_20_ = (packet.readUnsignedShort(842397944).let { i_5_ += it; i_5_ }.also { this.fileIds!![i_16_]!![i_19_] = it })
                if (i_18_ < i_20_) i_18_ = i_20_
                i_19_++
            }
            this.fileLimits!![i_16_] = i_18_ + 1
            if (1 + i_18_ == i_17_) this.fileIds!![i_16_] = null
            i_15_++
        }
        if (bool) {
            this.fileNames = arrayOfNulls<IntArray>(i_6_ + 1)
            this.fileNameTables = arrayOfNulls<NameHashTable>(1 + i_6_)
            for (i_21_ in 0..<this.groupCount) {
                val i_22_ = this.groupIds!![i_21_]
                val i_23_ = this.fileCounts!![i_22_]
                this.fileNames!![i_22_] = IntArray(this.fileLimits!![i_22_])
                var i_24_ = 0
                while (this.fileLimits!![i_22_] > i_24_) {
                    this.fileNames!![i_22_]!![i_24_] = -1
                    i_24_++
                }
                var i_25_ = 0
                while (i_23_ > i_25_) {
                    val i_26_: Int
                    if (this.fileIds!![i_22_] != null) i_26_ = (this.fileIds!![i_22_]!![i_25_])
                    else i_26_ = i_25_
                    this.fileNames!![i_22_]!![i_26_] = packet.readInt((-126).toByte())
                    i_25_++
                }
                this.fileNameTables!![i_22_] = NameHashTable(this.fileNames!![i_22_]!!)
            }
        }
    }

    init {
        try {
            this.crc = Class59_Sub1.method554(5126, `is`!!.size, `is`)
            if (i != this.crc) throw RuntimeException()
            if (is_27_ != null) {
                if (is_27_.size != 64) throw RuntimeException()
                whirlpool = Class348_Sub1_Sub2.method2730(4567, 0, `is`, `is`.size)
                for (i_28_ in 0..63) {
                    if (whirlpool!![i_28_] != is_27_[i_28_]) throw RuntimeException()
                }
            }
            loadIndex((-120).toByte(), `is`)
        } catch (runtimeexception: RuntimeException) {
            throw Class348_Sub17.method2929(runtimeexception, ("vw.<init>(" + (if (`is` != null) "{...}" else "null") + ',' + i + ',' + (if (is_27_ != null) "{...}" else "null") + ')'))
        }
    }

    companion object {
        var anInt3731: Int = 0
        var anInt3741: Int = 0
    }
}
