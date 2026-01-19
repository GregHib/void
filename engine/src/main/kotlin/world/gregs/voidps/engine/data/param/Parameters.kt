package world.gregs.voidps.engine.data.param

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.NpcType
import world.gregs.voidps.cache.type.Params
import world.gregs.voidps.engine.data.param.codec.ParamCodec
import world.gregs.voidps.engine.data.types.keys.NpcParams
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.set
import kotlin.concurrent.atomics.AtomicInt
import kotlin.reflect.KClass

abstract class Parameters<T : Params> {
    abstract val parameters: List<Triple<String, Int, ParamCodec<*>>>
//    abstract val parameters: Map<Int, ParamCodec<*>>
    private lateinit var codecs: Map<Int, ParamCodec<*>>
    private lateinit var keys: Map<String, Int>

    fun validate() {
        val set = mutableSetOf<Int>()
        val codecs = Int2ObjectOpenHashMap<ParamCodec<*>>(parameters.size)
        val keys = Object2IntOpenHashMap<String>(parameters.size)
        for ((key, id, codec) in parameters) {
            require(set.add(id)) { "Duplicate parameter id: $id" }
            require(id < Short.MAX_VALUE) { "Parameter id out of bounds: $id"}
            codecs.put(id, codec)
            keys.put(key, id)
        }
        this.codecs = codecs
        this.keys = keys
    }

    fun read(config: ConfigReader): MutableMap<Int, Any> {
        val params = Int2ObjectOpenHashMap<Any>()
        while (config.nextPair()) {
            val key = config.key()
            val id = keys[key] ?: throw IllegalArgumentException("Unknown key: $key")
            val codec = codecs[id] ?: throw IllegalArgumentException("Unknown codec: $id")
            params[id] = codec.read(config)
        }
        return params
    }

    /*
        Writing = 60ms
        Read both = 200ms
        Read definitions 54ms
        Array type Conversion 13ms
        Read config = 100ms

        Pure cache = 90ms
     */

    fun read(file: File, types: Array<T>) {
        val start = System.currentTimeMillis()
        val array = file.readBytes()
        println("Array read took ${System.currentTimeMillis() - start}ms ${array.size}")
        val reader = ArrayReader(array)
        for (type in types) {
            type.stringId = reader.readString()
            val params = NpcParams.read(reader) ?: continue
            type.set(params)
        }
    }

    fun read(reader: Reader): Map<Int, Any>? {
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

    fun write(writer: Writer, params: Map<Int, Any>?) {
        if (params == null) {
            writer.writeByte(0)
            return
        }
        writer.writeByte(params.count { it.key >= 0 })
        for (id in params.keys) {
            if (id < 0) {
                continue
            }
            val codec = codecs[id] ?: throw IllegalArgumentException("Unknown key id: $id")
            writer.writeShort(id)
            codec.write(writer, params[id]!!)
        }
    }

    companion object {
        const val CLONE = -2
        const val ID = -1
    }
}