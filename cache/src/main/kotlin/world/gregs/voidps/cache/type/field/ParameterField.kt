package world.gregs.voidps.cache.type.field

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writePair
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.type.TypeDecoder
import world.gregs.voidps.cache.type.TypeField
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
    private val renames: Map<String, String> = emptyMap(),
    private val originals: Map<String, String> = emptyMap(),
) : TypeField(paramIds.keys.toList()) {
    internal var value: Map<String, Any>? = null

    override fun write(writer: Writer, opcode: Int): Boolean {
        val value = value
        if (value == null || value.isEmpty()) {
            return false
        }
        writer.writeByte(opcode)
        writer.writeByte(value.size)
        for ((key, value) in value) {
            val original = originals[key] ?: key
            val reversed = transforms[original]?.binaryEncode?.invoke(value) ?: value
            writer.writeByte(reversed is String)
            val id = paramIds[original] ?: throw IllegalArgumentException("Unknown parameter type $original")
            writer.writeMedium(id)
            when (reversed) {
                is String -> writer.writeString(reversed)
                is Int -> writer.writeInt(reversed)
                else -> throw IllegalArgumentException("Invalid parameter $original type ${reversed::class.simpleName}: $reversed")
            }
        }
        return true
    }

    override fun read(reader: Reader, opcode: Int) {
        val size = reader.readUnsignedByte()
        if (size == 0) {
            value = null
            return
        }
        val extras = Object2ObjectOpenHashMap<String, Any>(4, Hash.VERY_FAST_LOAD_FACTOR)
        for (i in 0 until size) {
            val string = reader.readUnsignedBoolean()
            val id = reader.readUnsignedMedium()
            val name = params.getOrDefault(id, id.toString())
            val renamed = renames[name] ?: name
            val value = if (string) reader.readString() else reader.readInt()
            val transformed = transforms[renamed]?.binaryDecode?.invoke(value) ?: value
            extras[renamed] = transformed
        }
        value = extras
    }

    override fun read(reader: ConfigReader, key: String) {
        if (value == null) {
            value = Object2ObjectOpenHashMap(4, Hash.VERY_FAST_LOAD_FACTOR)
        }
        val renamed = renames[key] ?: key
        val value = reader.value()
        val transformed = transforms[renamed]?.configDecode?.invoke(value) ?: value
        (this.value as MutableMap<String, Any>)[renamed] = transformed
    }

    override fun write(writer: ConfigWriter, key: String) {
        val value = value?.get(key) ?: return
        val original = originals[key] ?: key
        val reversed = transforms[original]?.configEncode?.invoke(value) ?: value
        writer.writePair(original, reversed)
    }

    override fun reset() {
        value = null
    }

    @Suppress("UNCHECKED_CAST")
    override fun set(index: Int, value: Any?) {
        this.value = value as? Map<String, Any>
    }

}