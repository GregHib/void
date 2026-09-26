package world.gregs.voidps.tools.render

import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.Cache

/* Class189 */

class BillboardType {
    var aBoolean2522: Boolean = false
    var texture: Int
    var anInt2526: Int = 64
    var anInt2530: Int = 64
    var aBoolean2531: Boolean = false
    var anInt2533: Int = 1
    var anInt2534: Int = 2

    private fun method1418(i: Int, packet: Packet) {
        if (i == 1) {
            this.texture = packet.readUnsignedShort()
            if (this.texture == 65535) this.texture = -1
        } else if (i == 2) {
            this.anInt2526 = 1 + packet.readUnsignedShort()
            this.anInt2530 = packet.readUnsignedShort() - -1
        } else if (i != 3) {
            if (i == 4) this.anInt2534 = packet.readUnsignedByte()
            else if (i != 5) {
                if (i == 6) this.aBoolean2522 = true
                else if (i == 7) this.aBoolean2531 = true
            } else this.anInt2533 = packet.readUnsignedByte()
        } else packet.readByte()
    }

    fun method1419(packet: Packet) {
        while (true) {
            val i_4_ = packet.readUnsignedByte()
            if (i_4_ == 0) break
            method1418(i_4_, packet)
        }
    }

    init {
        this.texture = -1
    }

    companion object {
        /** Class73.cache */
        var cache: Cache? = null

        /** Class73.aClass60_2844 */
        var aReferenceCache_2844: ReferenceCache = ReferenceCache(64)

        /** Class73.list */
        fun list(i_0_: Int): BillboardType {
            var billboardType = aReferenceCache_2844.method583(i_0_.toLong()) as BillboardType?
            if (billboardType != null) return billboardType
            val `is` = cache!!.data(Index.BILLBOARDS, 0, i_0_)
            billboardType = BillboardType()
            if (`is` != null) billboardType.method1419(Packet(`is`))
            aReferenceCache_2844.method582(billboardType, i_0_.toLong())
            return billboardType
        }
    }
}
