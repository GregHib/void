@file:OptIn(ExperimentalSerializationApi::class)

package world.gregs.voidps.cache.format.definition.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.internal.TaggedDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
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
        for (index in 0 until elements) {
            val tag = descriptor.getOperationOrNull(index) ?: -1
            if (tag != -1) {
                indices.getOrPut(tag) { mutableListOf() }.add(index)
            }
            val value = descriptor.getSetterOrNull(index) ?: -1
            if (value != -1L) {
                setters[index] = value
            }
        }
        indexCache = indices.mapValues { (_, value) -> value.reversed().toIntArray() }
        setterCache = setters
    }

    private fun getIndexByTag(tag: Int, index: Int): Int {
        return indexCache[tag]?.getOrNull(index) ?: -1
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


    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return when(descriptor.kind) {
            StructureKind.LIST -> {
                println("List structure ${descriptor.getElementAnnotations(0)} ${descriptor.getElementDescriptor(0).annotations}")
                ListDecoder(reader, popTag())
            }
            else -> this
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {
    }

    override fun decodeTaggedValue(tag: Int): Any {
        println("Decode value $tag")
        return super.decodeTaggedValue(tag)
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        println("Decode collection size")
        return super.decodeCollectionSize(descriptor)
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
            println("Decode element index $opcode $index")
        }
        val index = getIndexByTag(opcode, --index)
        if (index == -1) {
            println("Unable to find tag for $opcode ${indexCache[opcode]?.contentToString()}")
            return CompositeDecoder.UNKNOWN_NAME
        }
        return index
    }

    override fun SerialDescriptor.getTag(index: Int): Int = index

}

class ListDecoder(val reader: Reader, val index: Int, var elementsCount: Int = 0) : AbstractDecoder() {
    private var elementIndex = 0

    override val serializersModule: SerializersModule = EmptySerializersModule

    override fun decodeValue(): Any = reader.readInt()

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (elementIndex == elementsCount) return CompositeDecoder.DECODE_DONE
        return elementIndex++
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder =
        ListDecoder(reader, descriptor.elementsCount)

    override fun decodeSequentially(): Boolean = true

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        println("Decode collection size $index ${descriptor.getElementAnnotations(0)} ${descriptor.annotations}")
        return decodeByte().toInt().also { elementsCount = it }
    }
}