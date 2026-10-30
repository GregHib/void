package world.gregs.voidps.tools.render

/* Class4 */

/**
 * A single animation frame: a list of transforms, each referencing a group in [base].
 */
class AnimationFrame(data: ByteArray, val base: AnimationFrameBase) {
    var count = 0
        private set

    /** Index into [AnimationFrameBase] of each transform. */
    var groups = ShortArray(0)
        private set

    /** Index of the origin group to reset before a translate/rotate/scale, or -1. */
    var origins = ShortArray(0)
        private set
    var x = ShortArray(0)
        private set
    var y = ShortArray(0)
        private set
    var z = ShortArray(0)
        private set
    var flags = ByteArray(0)
        private set

    /** Frame modifies face alpha (Class4.aBoolean139). */
    var alpha = false
        private set

    /** Frame modifies face colours (Class4.aBoolean131). */
    var colour = false
        private set

    /** Frame modifies billboards (Class4.aBoolean129). */
    var billboards = false
        private set

    init {
        val groupBuffer = ShortArray(500)
        val xBuffer = ShortArray(500)
        val yBuffer = ShortArray(500)
        val zBuffer = ShortArray(500)
        val originBuffer = ShortArray(500)
        val flagBuffer = ByteArray(500)
        try {
            val packet = Packet(data)
            val values = Packet(data)
            packet.readUnsignedByte()
            packet.pos += 2
            val length = packet.readUnsignedByte()
            var count = 0
            var lastOrigin = -1
            var usedOrigin = -1
            values.pos = packet.pos + length
            for (i in 0 until length) {
                val type = base.types[i]
                if (type == 0) lastOrigin = i
                val attributes = packet.readUnsignedByte()
                if (attributes <= 0) continue
                if (type == 0) usedOrigin = i
                groupBuffer[count] = i.toShort()
                val default: Short = if (type == 3 || type == 10) 128 else 0
                xBuffer[count] = if (attributes and 0x1 != 0) values.method3362().toShort() else default
                yBuffer[count] = if (attributes and 0x2 != 0) values.method3362().toShort() else default
                zBuffer[count] = if (attributes and 0x4 != 0) values.method3362().toShort() else default
                flagBuffer[count] = (attributes ushr 3 and 0x3).toByte()
                if (type == 2 || type == 9) {
                    xBuffer[count] = (xBuffer[count].toInt() shl 2 and 0x3fff).toShort()
                    yBuffer[count] = (yBuffer[count].toInt() shl 2 and 0x3fff).toShort()
                    zBuffer[count] = (zBuffer[count].toInt() shl 2 and 0x3fff).toShort()
                }
                originBuffer[count] = -1
                if (type == 1 || type == 2 || type == 3) {
                    if (lastOrigin > usedOrigin) {
                        originBuffer[count] = lastOrigin.toShort()
                        usedOrigin = lastOrigin
                    }
                } else if (type == 5) {
                    alpha = true
                } else if (type == 7) {
                    colour = true
                } else if (type == 9 || type == 10 || type == 8) {
                    billboards = true
                }
                count++
            }
            if (values.pos != data.size) throw RuntimeException()
            this.count = count
            groups = groupBuffer.copyOf(count)
            x = xBuffer.copyOf(count)
            y = yBuffer.copyOf(count)
            z = zBuffer.copyOf(count)
            origins = originBuffer.copyOf(count)
            flags = flagBuffer.copyOf(count)
        } catch (exception: Exception) {
            this.count = 0
            alpha = false
            colour = false
        }
    }
}
