package world.gregs.voidps.cache.definition

import world.gregs.voidps.buffer.read.Reader

interface Transforms {
    var varbit: Int
    var varp: Int
    var transforms: IntArray?

    fun readTransforms(buffer: Reader, isLast: Boolean) {
        varbit = buffer.readShort()
        if (varbit == 65535) {
            varbit = -1
        }
        varp = buffer.readShort()
        if (varp == 65535) {
            varp = -1
        }
        var last = -1
        if (isLast) {
            last = buffer.readUnsignedShort()
            if (last == 65535) {
                last = -1
            }
        }
        val length = buffer.readUnsignedByte()
        transforms = IntArray(length + 2)
        for (count in 0..length) {
            transforms!![count] = buffer.readUnsignedShort()
            if (transforms!![count] == 65535) {
                transforms!![count] = -1
            }
        }
        transforms!![length + 1] = last
    }
}