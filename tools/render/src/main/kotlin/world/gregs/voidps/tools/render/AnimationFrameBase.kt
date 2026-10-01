package world.gregs.voidps.tools.render

/* Class348_Sub33 */

/**
 * An animation skeleton: groups of vertex/face labels and the transform type applied to each group.
 *
 * Types: 0 origin, 1 translate, 2 rotate, 3 scale, 5 alpha, 7 colour, 8/9/10 billboard translate/rotate/scale.
 */
class AnimationFrameBase(val id: Int, data: ByteArray) {
    val count: Int
    val types: IntArray
    val animatable: BooleanArray
    val partMasks: IntArray
    val labels: Array<IntArray>

    init {
        val packet = Packet(data)
        count = packet.readUnsignedByte()
        types = IntArray(count)
        animatable = BooleanArray(count)
        partMasks = IntArray(count)
        for (i in 0 until count) {
            types[i] = packet.readUnsignedByte()
            if (types[i] == 6) types[i] = 2
        }
        for (i in 0 until count) animatable[i] = packet.readUnsignedByte() == 1
        for (i in 0 until count) partMasks[i] = packet.readUnsignedShort()
        labels = Array(count) { IntArray(packet.readUnsignedByte()) }
        for (i in 0 until count) {
            for (j in labels[i].indices) labels[i][j] = packet.readUnsignedByte()
        }
    }
}
