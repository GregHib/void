package world.gregs.voidps.engine.data.param

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.data.NpcType
import world.gregs.voidps.cache.type.Params
import world.gregs.voidps.engine.data.param.codec.ParamCodec
import world.gregs.voidps.engine.data.param.NpcParams
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.set
import kotlin.concurrent.atomics.AtomicInt
import kotlin.reflect.KClass

abstract class Parameters<T : Params> {
    internal abstract val codecs: Map<Int, ParamCodec<*>>
    internal abstract val keys: Map<String, Int>

    fun validate() {
        val set = mutableSetOf<Int>()
        for (id in keys.values) {
            require(set.add(id)) { "Duplicate parameter id: $id" }
            require(id < Short.MAX_VALUE) { "Parameter id out of bounds: $id"}
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

    fun read(file: File, types: Array<T>) {
        val array = file.readBytes()
        val reader = ArrayReader(array)
        for (type in types) {
            type.stringId = reader.readString()
            val params = read(reader) ?: continue
            type.set(params)
        }
    }

    private fun read(reader: Reader): Map<Int, Any>? {
        val size = reader.readByte()
        if (size == 0) {
            return null
        }
        val params = Int2ObjectOpenHashMap<Any>()
        for (i in 0 until size) {
            val id = reader.readShort()
            val codec = codecs[id] ?: throw IllegalArgumentException("Unknown key id: $id")
            params[id] = codec.read(reader)
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

    private fun write(writer: Writer, params: Map<Int, Any>?) {
        if (params == null) {
            writer.writeByte(0)
            return
        }
        writer.writeByte(params.count { codecs.containsKey(it.key) })
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
    }
}