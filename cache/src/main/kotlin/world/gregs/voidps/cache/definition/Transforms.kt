package world.gregs.voidps.cache.definition

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Definition

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

    companion object {
        val transformer = { array: Array<out Definition>, names: Map<Int, String> ->
            for (def in array) {
                if (def is Transforms) {
                    def.transforms = def.transformIds?.map { if (it == -1) null else names.getOrDefault(it, it.toString()) }?.toTypedArray()
                }
            }
        }
    }
}