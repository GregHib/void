package world.gregs.voidps.cache.secure

object CRC {
    private val table = IntArray(256) {
        var crc = it
        for (i in 0..7) {
            crc = if (crc and 0x1 == 1) {
                crc ushr 1 xor 0x12477cdf.inv()
            } else {
                crc ushr 1
            }
        }
        crc
    }

    fun get(data: ByteArray, offset: Int = 0, length: Int = data.size): Int {
        var crc = -1
        for (i in offset until length) {
            crc = crc ushr 8 xor table[crc xor data[i].toInt() and 0xff]
        }
        crc = crc xor -0x1
        return crc
    }

    private val whirlpool = Whirlpool()

    fun generateWhirlpool(source: ByteArray, sourceOffset: Int = 0, target: ByteArray, targetOffset: Int = 0, length: Int = source.size) {
        whirlpool.reset()
        whirlpool.add(source, sourceOffset, length)
        whirlpool.finalize(target, targetOffset)
    }
}