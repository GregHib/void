@file:OptIn(ExperimentalSerializationApi::class)

package world.gregs.voidps.cache.format.definition.internal

import com.fasterxml.jackson.databind.ser.std.StdArraySerializers
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.internal.TaggedDecoder
import world.gregs.voidps.buffer.DataType
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.format.definition.Medium

@OptIn(InternalSerializationApi::class)
internal class DefinitionDecoder(
    bytes: ByteArray,
    descriptor: SerialDescriptor
) : TaggedDecoder<Int>() {

    private lateinit var indexedOperationsCache: Map<Int, Pair<Int, Int>>
    private lateinit var indexCache: Map<Int, IntArray>
    private lateinit var setterCache: Map<Int, Long>
    val reader: Reader = BufferReader(bytes)

    var opcode = 0
    var index = 0

    init {
        populateCache(descriptor)
    }

    private fun populateCache(descriptor: SerialDescriptor) {
        val elements = descriptor.elementsCount
        val indices = mutableMapOf<Int, MutableList<Int>>()
        val setters = mutableMapOf<Int, Long>()
        val indexedMap = mutableMapOf<Int, Pair<Int, Int>>()
        for (index in 0 until elements) {
            val opcode = descriptor.getOperationOrNull(index)
            if (opcode != null) {
                indices.getOrPut(opcode) { mutableListOf() }.add(index)
            }
            val value = descriptor.getSetterOrNull(index)
            if (value != null) {
                setters[index] = value
            }
            val indexed = descriptor.getIndexedOrNull(index)
            if (indexed != null) {
                val size = indexed.operations.size
                indexed.operations.forEachIndexed { i, code ->
                    indices.getOrPut(code) { mutableListOf() }.add(index)
                    indexedMap[code] = i to size
                }
            }
        }
        indexCache = indices.mapValues { (_, value) -> value.reversed().toIntArray() }
        setterCache = setters
        indexedOperationsCache = indexedMap
    }

    private fun getIndexByCode(opcode: Int, index: Int): Int {
        return indexCache[opcode]?.getOrNull(index) ?: -1
    }

    private fun getSetter(index: Int): Long? {
        return setterCache[index]
    }

    private fun readType(type: DataType): Long {
        return reader.readSigned(type, Modifier.NONE, Endian.BIG)
    }

    private fun getIndicesByTag(tag: Int): Int {
        return indexCache[tag]?.size ?: 0
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>, previousValue: T?): T {
        val operations = indexedOperationsCache[opcode]
        if (operations != null) {
            return decodeIndexedArray(deserializer.descriptor, previousValue, operations) as T
        }
        return super.decodeSerializableValue(deserializer, previousValue)
    }

    private fun decodeIndexedArray(descriptor: SerialDescriptor, previousValue: Any?, operations: Pair<Int, Int>): Any {
        return when (val name = descriptor.serialName) {
            "kotlin.ByteArray" -> (previousValue as? ByteArray ?: ByteArray(operations.second)).apply { this[operations.first] = reader.readByte().toByte() }
            "kotlin.UByteArray" -> (previousValue as? UByteArray ?: UByteArray(operations.second)).apply { this[operations.first] = reader.readByte().toUByte() }
            "kotlin.ShortArray" -> (previousValue as? ShortArray ?: ShortArray(operations.second)).apply { this[operations.first] = reader.readShort().toShort() }
            "kotlin.UShortArray" -> (previousValue as? UShortArray ?: UShortArray(operations.second)).apply { this[operations.first] = reader.readShort().toUShort() }
            "kotlin.IntArray" -> (previousValue as? IntArray ?: IntArray(operations.second)).apply { this[operations.first] = reader.readInt() }
            "kotlin.UIntArray" -> (previousValue as? UIntArray ?: UIntArray(operations.second)).apply { this[operations.first] = reader.readInt().toUInt() }
            "kotlin.LongArray" -> (previousValue as? LongArray ?: LongArray(operations.second)).apply { this[operations.first] = reader.readLong() }
            "kotlin.ULongArray" -> (previousValue as? ULongArray ?: ULongArray(operations.second)).apply { this[operations.first] = reader.readLong().toULong() }
            "kotlin.Array" -> (previousValue as? Array<String?> ?: arrayOfNulls<String?>(operations.second)).apply { this[operations.first] = reader.readString() }
            else -> throw UnsupportedOperationException("Unknown indexed array type $name")
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {
    }

    override fun decodeTaggedBoolean(tag: Int): Boolean = decodeTaggedByte(tag).toInt() == 1
    override fun decodeTaggedByte(tag: Int): Byte = getSetter(tag)?.toByte() ?: readType(DataType.BYTE).toByte()
    override fun decodeTaggedShort(tag: Int): Short = getSetter(tag)?.toShort() ?: readType(DataType.SHORT).toShort()
    override fun decodeTaggedInt(tag: Int): Int = getSetter(tag)?.toInt() ?: readType(DataType.INT).toInt()
    override fun decodeTaggedLong(tag: Int): Long = getSetter(tag) ?: readType(DataType.LONG)
    override fun decodeTaggedString(tag: Int): String = reader.readString()

    fun decodeTaggedMedium(tag: Int): Medium = Medium((getSetter(tag) ?: readType(DataType.MEDIUM)).toInt())
    fun decodeMedium(): Medium = decodeTaggedMedium(popTag())

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (index <= 0) {
            if (!reader.buffer.hasRemaining()) {
                return CompositeDecoder.DECODE_DONE
            }
            opcode = reader.readByte()
            if (opcode == 0) {
                return CompositeDecoder.DECODE_DONE
            }
            index = getIndicesByTag(opcode)
        }
        val index = getIndexByCode(opcode, --index)
        if (index == -1) {
            println("Unable to find tag for $opcode ${indexCache[opcode]?.contentToString()}")
            return CompositeDecoder.UNKNOWN_NAME
        }
        return index
    }

    override fun SerialDescriptor.getTag(index: Int): Int = index

}