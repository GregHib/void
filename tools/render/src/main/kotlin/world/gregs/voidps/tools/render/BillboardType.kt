package world.gregs.voidps.tools.render

/* Class189 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

class BillboardType {
    /* Minimal stub: Class64_Sub1's kept code (method623 area) only reads
     * these instance fields off a Class189 obtained from Class73.method742(...)
     * and constructs a Class6 from them. method1418/method1419 are restored
     * below because Class73.method742 (genuine) itself calls method1419 to
     * decode the cached data before returning it. */
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
}
