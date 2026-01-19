package world.gregs.voidps.engine.data.param

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.Params
import world.gregs.voidps.engine.data.param.codec.ParamCodec
import world.gregs.voidps.engine.data.types.keys.NpcParams
import java.io.File
import kotlin.collections.set

abstract class Parameters<T : Params> {
    abstract val parameters: Map<Int, ParamCodec<*>>
    abstract val keys: Map<String, Int>

    fun validate() {
        val set = mutableSetOf<Int>()
        for (id in keys.values) {
            require(set.add(id)) { "Duplicate parameter id: $id" }
        }
    }

    fun read(config: ConfigReader): MutableMap<Int, Any> {
        val params = Int2ObjectOpenHashMap<Any>()
        while (config.nextPair()) {
            val key = config.key()
            val id = keys[key] ?: throw IllegalArgumentException("Unknown key: $key")
            val codec = parameters[id] ?: throw IllegalArgumentException("Unknown codec: $id")
            params[codec.id] = codec.read(config)
        }
        return params
    }

    fun read(file: File, types: Array<T>) {
        val reader = ArrayReader(file.readBytes())
        for (type in types) {
            type.stringId = reader.readString()
            val params = NpcParams.read(reader) ?: continue
            type.set(params)
        }
    }

    fun read(reader: Reader): Map<Int, Any>? {
        val size = reader.readShort()
        if (size == 0) {
            return null
        }
        val params = Int2ObjectOpenHashMap<Any>()
        for (i in 0 until size) {
            val id = reader.readUnsignedMedium()
            val codec = parameters[id] ?: throw IllegalArgumentException("Unknown key id: $id")
            params[codec.id] = codec.read(reader)
        }
        return params
    }

    fun write(file: File, types: Array<T>, size: Int = 100_000) {
        val writer = ArrayWriter(size)
        for (type in types) {
            writer.writeString(type.stringId)
            write(writer, type.params)
        }
        file.writeBytes(writer.toArray())
    }

    fun write(writer: Writer, params: Map<Int, Any>?) {
        if (params == null) {
            writer.writeShort(0)
            return
        }
        writer.writeShort(params.count { it.key >= 0 })
        for (id in params.keys) {
            if (id < 0) {
                continue
            }
            val codec = parameters[id] ?: throw IllegalArgumentException("Unknown key id: $id")
            codec.write(writer, params[id]!!)

        }
    }

    companion object {
        const val CLONE = -2
        const val ID = -1
    }
}