package world.gregs.config.param

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.ConfigReader
import world.gregs.config.param.codec.ParamCodec
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.buffer.write.Writer
import java.io.File
import kotlin.collections.set

abstract class Parameters {
    internal abstract val codecs: Map<Int, ParamCodec<*>>
    internal abstract val keys: Map<String, Int>

    fun id(key: String): Int = keys[key] ?: throw IllegalArgumentException("Unknown key: $key")

    fun validate() {
        val set = mutableSetOf<Int>()
        for (id in keys.values) {
            require(set.add(id)) { "Duplicate parameter id: $id" }
            require(id < Short.MAX_VALUE) { "Parameter id out of bounds: $id" }
        }
    }

    fun read(config: ConfigReader): MutableMap<Int, Any> {
        val params = Int2ObjectOpenHashMap<Any>()
        while (config.nextPair()) {
            val key = config.key()
            val id = keys[key] ?: throw IllegalArgumentException("Unknown key: $key")
            val codec = codecs[id] ?: throw IllegalArgumentException("Unknown id: $id")
            params[id] = codec.read(config)
        }
        return params
    }

    fun <T : Param> read(file: File, types: Array<T>): MutableMap<String, Int> {
        val array = file.readBytes()
        val reader = ArrayReader(array)
        val ids = Object2IntOpenHashMap<String>(20_000)
        ids.defaultReturnValue(-1)
        while (reader.position < reader.length) {
            val id = reader.readInt()
            val type = types[id]
            type.stringId = reader.readString()
            ids[type.stringId] = type.id
            val params = read(reader)
            type.params = params
        }
        return ids
    }

    fun read(reader: Reader): Map<Int, Any>? {
        val size = reader.readByte()
        if (size == 0) {
            return null
        }
        val params = Int2ObjectOpenHashMap<Any>(size)
        for (i in 0 until size) {
            val id = reader.readShort()
            val codec = codecs[id] ?: throw IllegalArgumentException("Unknown key id: $id")
            params[id] = codec.read(reader)
        }
        return params
    }

    fun <T : Param> write(file: File, types: Array<T>, size: Int = 100_000) {
        val writer = ArrayWriter(size)
        for (type in types) {
            writer.writeString(type.stringId)
            write(writer, type.params)
        }
        file.writeBytes(writer.toArray())
    }

    fun write(writer: Writer, params: Map<Int, Any>?) {
        if (params == null) {
            return
        }
        writer.writeByte(params.count { codecs.containsKey(it.key) && it.key >= 0 })
        for (id in params.keys) {
            if (id < 0) {
                continue
            }
            val codec = codecs[id] ?: continue
            writer.writeShort(id)
            codec.write(writer, params[id]!!)
        }
    }

    companion object {
        const val CLONE = -2
        const val ID = -1
        const val STRING_ID = -3
    }
}