package world.gregs.voidps.cache.definition.decoder

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices.ENUMS
import world.gregs.voidps.cache.definition.data.EnumDefinition

class EnumDecoder(cache: Cache) : DefinitionDecoder<EnumDefinition>(cache, ENUMS) {

    override fun create() = EnumDefinition()

    override fun getFile(id: Int) = id and 0xff

    override fun getArchive(id: Int) = id ushr 8

    override fun EnumDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> keyType = byteToChar(buffer.readByte().toByte())
            2 -> valueType = byteToChar(buffer.readByte().toByte())
            3 -> defaultString = buffer.readString()
            4 -> defaultInt = buffer.readInt()
            5, 6 -> {
                length = buffer.readShort()
                val hashtable = Int2ObjectArrayMap<Any>()
                repeat(length) {
                    val id = buffer.readInt()
                    hashtable[id] = if (opcode == 5) {
                        buffer.readString()
                    } else {
                        buffer.readInt()
                    }
                }
                map = hashtable
            }
            7 -> {
                val size = buffer.readShort()
                length = buffer.readShort()
                val strings = Int2ObjectArrayMap<Any>(size)
                repeat(length) {
                    val index = buffer.readShort()
                    strings[index] = buffer.readString()
                }
                map = strings
            }
            8 -> {
                val size = buffer.readShort()
                length = buffer.readShort()
                val integers = Int2ObjectArrayMap<Any>(size)
                repeat(length) {
                    val index = buffer.readShort()
                    integers[index] = buffer.readInt()
                }
                map = integers
            }
        }
    }
}