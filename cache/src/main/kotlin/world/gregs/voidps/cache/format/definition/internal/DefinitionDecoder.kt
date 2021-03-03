package world.gregs.voidps.cache.format.definition.internal

/*@file:OptIn(ExperimentalSerializationApi::class)

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.ShortArraySerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.internal.TaggedDecoder
import kotlinx.serialization.serializer
import world.gregs.voidps.buffer.DataType
import world.gregs.voidps.buffer.Endian
import world.gregs.voidps.buffer.Modifier
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.definition.data.ItemDefinition2
import world.gregs.voidps.cache.format.definition.Medium


@OptIn(InternalSerializationApi::class)
internal class DefinitionDecoder(
    bytes: ByteArray,
    descriptor: SerialDescriptor
) : TaggedDecoder<Int>() {

    private data class IndexedArray(val index: Int, val size: Int)

    private lateinit var indexedArraysCache: Map<Int, IndexedArray>
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
        val indexedMap = mutableMapOf<Int, IndexedArray>()
        for (index in 0 until elements) {
            val opcode = descriptor.getOperationOrNull(index)
            if (opcode != null) {
                indices.getOrPut(opcode) { mutableListOf() }.add(index)

                val value = descriptor.getSetterOrNull(index)
                if (value != null) {
                    setters[index] = value
                }
            } else {
                val indexed = descriptor.getIndexedOrNull(index)
                if (indexed != null) {
                    val size = indexed.operations.size
                    indexed.operations.forEachIndexed { i, code ->
                        indices.getOrPut(code) { mutableListOf() }.add(index)
                        indexedMap[code] = IndexedArray(i, size)
                    }
                }
            }
        }
        indexCache = indices.mapValues { (_, value) -> value.reversed().toIntArray() }
        setterCache = setters
        indexedArraysCache = indexedMap
    }

    private fun getIndexByCode(opcode: Int, index: Int): Int {
        return indexCache[opcode]?.getOrNull(index) ?: -1
    }

    private fun getSetter(index: Int): Long? {
        return if (index < 0) null else setterCache[index]
    }

    private fun readType(type: DataType): Long {
        return reader.readSigned(type).apply {
        }
    }

    private fun getIndicesByTag(tag: Int): Int {
        return indexCache[tag]?.size ?: 0
    }
    private val byteArraySerializer = serializer<ByteArray>()
    private val shortArraySerializer = serializer<ByteArray>()
    @Suppress("UNCHECKED_CAST")
    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>, previousValue: T?): T {
        val operations = indexedArraysCache[opcode]
        if (operations != null) {
            return decodeIndexedArray(deserializer.descriptor, previousValue, operations) as T
        }
        if (deserializer === byteArraySerializer) {
            return ByteArray(reader.readByte()) { reader.readByte().toByte() } as T
        }
        if (deserializer === shortArraySerializer) {
            return ShortArray(reader.readByte()) { reader.readShort().toShort() } as T
        }
        return super.decodeSerializableValue(deserializer, previousValue)
    }

    private fun decodeIndexedArray(descriptor: SerialDescriptor, previousValue: Any?, array: IndexedArray): Any {
        return when (val name = descriptor.serialName) {
            "kotlin.ByteArray" -> (previousValue as? ByteArray ?: ByteArray(array.size)).apply { this[array.index] = reader.readByte().toByte() }
            "kotlin.UByteArray" -> (previousValue as? UByteArray ?: UByteArray(array.size)).apply { this[array.index] = reader.readByte().toUByte() }
            "kotlin.ShortArray" -> (previousValue as? ShortArray ?: ShortArray(array.size)).apply { this[array.index] = reader.readShort().toShort() }
            "kotlin.UShortArray" -> (previousValue as? UShortArray ?: UShortArray(array.size)).apply { this[array.index] = reader.readShort().toUShort() }
            "kotlin.IntArray" -> (previousValue as? IntArray ?: IntArray(array.size)).apply { this[array.index] = reader.readInt() }
            "kotlin.UIntArray" -> (previousValue as? UIntArray ?: UIntArray(array.size)).apply { this[array.index] = reader.readInt().toUInt() }
            "kotlin.LongArray" -> (previousValue as? LongArray ?: LongArray(array.size)).apply { this[array.index] = reader.readLong() }
            "kotlin.ULongArray" -> (previousValue as? ULongArray ?: ULongArray(array.size)).apply { this[array.index] = reader.readLong().toULong() }
            "kotlin.Array" -> (previousValue as? Array<String?> ?: arrayOfNulls<String?>(array.size)).apply { this[array.index] = reader.readString() }
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
            opcode = reader.readUnsignedByte()
            if (opcode == 0) {
                return CompositeDecoder.DECODE_DONE
            }
            index = getIndicesByTag(opcode)
        }
        val index = getIndexByCode(opcode, --index)
        if (index == -1) {
            println("Unknown field $opcode $index")
            return CompositeDecoder.UNKNOWN_NAME
        }
        return index
    }

    override fun SerialDescriptor.getTag(index: Int): Int = index

}*/
