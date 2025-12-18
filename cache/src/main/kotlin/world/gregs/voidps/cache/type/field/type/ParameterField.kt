package world.gregs.voidps.cache.type.field.type

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writePair
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.TypeDecoder
import world.gregs.voidps.cache.type.field.TypeField
import world.gregs.voidps.cache.type.field.codec.BooleanCodec
import world.gregs.voidps.cache.type.field.codec.DoubleCodec
import world.gregs.voidps.cache.type.field.codec.IntCodec
import world.gregs.voidps.cache.type.field.codec.LongCodec
import world.gregs.voidps.cache.type.field.codec.StringCodec
import kotlin.collections.iterator
import kotlin.collections.set

/**
 * Field for storing dynamic key-value parameters in a Type.
 *
 * Supports sparse storage of additional properties that aren't part of the main Type structure.
 * Values are stored as Map<String, Any> and can be strings or integers in binary format.
 *
 * Keys are mapped to integer IDs for efficient binary storage, and values can be transformed
 * between storage formats (e.g. storing doubles as integers in binary).
 */
class ParameterField(
    private val paramIds: Map<String, Int>,
    private val params: Map<Int, String>,
    private val transforms: Map<String, TypeDecoder.ParameterBuilder.Transform> = emptyMap(),
    private val transformIds: Map<Int, TypeDecoder.ParameterBuilder.Transform> = emptyMap(),
    private val renames: Map<String, String> = emptyMap(),
    private val originals: Map<String, String> = emptyMap(),
) : TypeField(paramIds.keys.toList()) {
    internal var value: MutableMap<Int, Any>? = null

    override fun join(other: TypeField) {
        other as ParameterField
        if (other.value == null) {
            return
        }
        if (value != null) {
            value!!.putAll(other.value!!)
        } else {
            value = other.value
        }
    }

    override fun writeBinary(writer: Writer, opcode: Int): Boolean {
        val value = value
        if (value == null || value.isEmpty()) {
            return false
        }
        writer.writeByte(opcode)
        writer.writeByte(value.size)
        for ((id, value) in value) {
            val key = params[id] ?: throw IllegalArgumentException("Unknown parameter id $id")
            val reversed = transformIds[id]?.binaryEncode?.invoke(value) ?: value
            writer.writeByte(
                when (reversed) {
                    is Double -> 4
                    is Boolean -> 3
                    is Long -> 2
                    is String -> 1
                    else -> 0
                }
            )
            writer.writeMedium(id)
            when (reversed) {
                is String -> StringCodec.writeBinary(writer, reversed)
                is Int -> IntCodec.writeBinary(writer, reversed)
                is Long -> LongCodec.writeBinary(writer, reversed)
                is Boolean -> BooleanCodec.writeBinary(writer, reversed)
                is Double -> DoubleCodec.writeBinary(writer, reversed)
                else -> throw IllegalArgumentException("Invalid parameter $key type ${reversed::class.simpleName}: $reversed")
            }
        }
        return true
    }

    override fun readBinary(reader: Reader, opcode: Int) {
        val size = reader.readUnsignedByte()
        if (size == 0) {
            value = null
            return
        }
        val extras = Int2ObjectOpenHashMap<Any>(size, Hash.VERY_FAST_LOAD_FACTOR)
        for (i in 0 until size) {
            val type = reader.readUnsignedByte()
            val id = reader.readUnsignedMedium()
            val value = when (type) {
                0 -> IntCodec.readBinary(reader)
                1 -> StringCodec.readBinary(reader)
                2 -> LongCodec.readBinary(reader)
                3 -> BooleanCodec.readBinary(reader)
                4 -> DoubleCodec.readBinary(reader)
                5 -> DoubleCodec.readBinary(reader)
                else -> throw IllegalArgumentException("Invalid parameter type $id: $type")
            }
            extras[id] = value
        }
        value = extras
    }

    override fun readConfig(reader: ConfigReader, key: String) {
        if (value == null) {
            value = Object2ObjectOpenHashMap(4, Hash.VERY_FAST_LOAD_FACTOR)
        }
        val renamed = renames[key] ?: key
        val id = paramIds[renamed] ?: paramIds[key] ?: throw IllegalArgumentException("Unknown parameter type $key")
        val value = reader.value()
        val transformed = transforms[renamed]?.configDecode?.invoke(value) ?: value
        (this.value as MutableMap<Int, Any>)[id] = transformed
    }

    override fun writeConfig(writer: ConfigWriter, key: String) {
        val renamed = renames[key] ?: key
        val id = paramIds[renamed] ?: paramIds[key] ?: throw IllegalArgumentException("Unknown parameter type $key")
        val value = value?.get(id) ?: return
        val original = originals[key] ?: key
        val reversed = transforms[original]?.configEncode?.invoke(value) ?: value
        writer.writePair(original, reversed)
    }

    override fun reset() {
        value = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParameterField

        return value == other.value
    }

    override fun hashCode(): Int {
        return value?.hashCode() ?: 0
    }

    override fun toString(): String {
        return value.toString()
    }

}