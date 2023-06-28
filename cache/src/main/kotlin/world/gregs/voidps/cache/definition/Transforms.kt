package world.gregs.voidps.cache.definition

import world.gregs.voidps.buffer.read.Reader

interface Transforms {
    var varbit: Int
    var varp: Int
    var transformIds: IntArray?
    var transforms: Array<String?>?

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
        transformIds = IntArray(length + 2)
        for (count in 0..length) {
            transformIds!![count] = buffer.readUnsignedShort()
            if (transformIds!![count] == 65535) {
                transformIds!![count] = -1
            }
        }
        transformIds!![length + 1] = last
    }
}