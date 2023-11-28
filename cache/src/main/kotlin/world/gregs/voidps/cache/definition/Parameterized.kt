package world.gregs.voidps.cache.definition

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

interface Parameterized {

    var params: Map<Long, Any>?

    fun readParameters(buffer: Reader) {
        val length = buffer.readUnsignedByte()
        if (length == 0) {
            return
        }
        val params = Long2ObjectArrayMap<Any>()
        for (i in 0 until length) {
            val string = buffer.readUnsignedBoolean()
            val id = buffer.readUnsignedMedium().toLong()
            params[id] = if (string) buffer.readString() else buffer.readInt()
        }
        this.params = params
    }

    fun writeParameters(writer: Writer) {
        params?.let { params ->
            writer.writeByte(249)
            writer.writeByte(params.size)
            params.forEach { (id, value) ->
                writer.writeByte(value is String)
                writer.writeMedium(id.toInt())
                if (value is String) {
                    writer.writeString(value)
                } else if (value is Int) {
                    writer.writeInt(value)
                }
            }
        }
    }
}