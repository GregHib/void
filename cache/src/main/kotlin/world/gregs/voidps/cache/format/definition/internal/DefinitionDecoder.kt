@file:OptIn(ExperimentalSerializationApi::class)

package world.gregs.voidps.cache.format.definition.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.format.definition.Definition

@OptIn(InternalSerializationApi::class)
internal open class DefinitionDecoder(
    private val definition: Definition,
    bytes: ByteArray,
    descriptor: SerialDescriptor
) : AbstractDecoder() {

    override val serializersModule: SerializersModule
        get() = definition.serializersModule

    private lateinit var indexCache: Map<Int, IntArray>
    val reader: Reader = BufferReader(bytes)

    var opcode = 0
    var index = 0

    init {
        populateCache(descriptor)
    }

    private fun populateCache(descriptor: SerialDescriptor) {
        val elements = descriptor.elementsCount
        val indices = mutableMapOf<Int, MutableList<Int>>()
        for (index in 0 until elements) {
            val tag = descriptor.getOperationOrNull(index) ?: -1
            if (tag != -1) {
                indices.getOrPut(tag) { mutableListOf() }.add(index)
            }
        }
        indexCache = indices.mapValues { (_, value) -> value.reversed().toIntArray() }
    }

    private fun getIndexByTag(tag: Int, index: Int): Int {
        return indexCache[tag]?.getOrNull(index) ?: -1
    }

    private fun getIndicesByTag(tag: Int): Int {
        return indexCache[tag]?.size ?: 0
    }

    override fun decodeString(): String {
        return reader.readString()
    }

    override fun decodeInt(): Int {
        return reader.readInt()
    }

    override fun decodeValue(): Any {
        println("Decode value")
        return super.decodeValue()
    }


    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        if (!reader.buffer.hasRemaining()) {
            return CompositeDecoder.DECODE_DONE
        }

        if (index <= 0) {
            opcode = reader.readByte()
            if (opcode == 0) {
                return CompositeDecoder.DECODE_DONE
            }
            index = getIndicesByTag(opcode)
        }
        val index = getIndexByTag(opcode, --index)
        if(index == -1) {
            println("Unable to find tag for $opcode ${indexCache[opcode]?.contentToString()}")
            return CompositeDecoder.UNKNOWN_NAME
        }
        return index
    }

}