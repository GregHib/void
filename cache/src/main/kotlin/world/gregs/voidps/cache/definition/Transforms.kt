package world.gregs.voidps.cache.definition

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

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

    fun writeTransforms(writer: Writer, smaller: Int, larger: Int) {
        val configIds = transforms
        if (configIds != null && (varbit != -1 || varp != -1)) {
            val last = configIds.last()
            val extended = last != -1
            writer.writeByte(if (extended) larger else smaller)
            writer.writeShort(varbit)
            writer.writeShort(varp)

            if (extended) {
                writer.writeShort(last)
            }
            writer.writeByte(configIds.size - 2)
            for (i in 0 until configIds.size - 1) {
                writer.writeShort(configIds[i])
            }
        }
    }
}
