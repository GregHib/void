package world.gregs.voidps.cache.definition

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer

@Suppress("UNCHECKED_CAST")
interface Parameterized {

    var params: HashMap<Long, Any>?

    fun <T : Any> getParam(key: Long) = params?.get(key) as T

    fun <T : Any> getParamOrNull(key: Long) = params?.get(key) as? T

    fun <T : Any> getParam(key: Long, default: T) = params?.get(key) as? T ?: default

    fun readParameters(buffer: Reader) {
        val length = buffer.readUnsignedByte()
        if (length == 0) {
            return
        }
        params = HashMap()
        repeat(length) {
            val string = buffer.readUnsignedBoolean()
            val id = buffer.readUnsignedMedium().toLong()
            params!![id] = if (string) buffer.readString() else buffer.readInt()
        }
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