package world.gregs.voidps.tools.icon

/* Class189 - Decompiled by JODE
* Visit http://jode.sourceforge.net/
*/

internal class BillboardType {
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

    private fun method1418(i: Int, i_0_: Int, packet: Packet, i_1_: Byte) {
        if (i_1_.toInt() != 94) this.anInt2526 = -81
        anInt2532++
        if (i == 1) {
            this.texture = packet.readUnsignedShort(842397944)
            if (this.texture == 65535) this.texture = -1
        } else if (i == 2) {
            this.anInt2526 = 1 + packet.readUnsignedShort(842397944)
            this.anInt2530 = packet.readUnsignedShort(842397944) - -1
        } else if (i != 3) {
            if (i == 4) this.anInt2534 = packet.readUnsignedByte(255)
            else if (i != 5) {
                if (i == 6) this.aBoolean2522 = true
                else if (i == 7) this.aBoolean2531 = true
            } else this.anInt2533 = packet.readUnsignedByte(255)
        } else packet.readByte(-106)
    }

    fun method1419(i: Int, packet: Packet, i_2_: Byte) {
        anInt2528++
        while (true) {
            val i_4_ = packet.readUnsignedByte(255)
            if (i_4_ == 0) break
            method1418(i_4_, i, packet, 94.toByte())
        }
    }

    init {
        this.texture = -1
    }

    companion object {
        var anInt2528: Int = 0
        var anInt2532: Int = 0
    }
}
