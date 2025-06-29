package world.gregs.voidps.cache.definition.decoder

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.ENUMS
import world.gregs.voidps.cache.definition.data.EnumDefinition

class EnumDecoder : DefinitionDecoder<EnumDefinition>(ENUMS) {

    override fun create(size: Int) = Array(size) { EnumDefinition(it) }

    override fun getFile(id: Int) = id and 0xff

    override fun getArchive(id: Int) = id ushr 8

    override fun EnumDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> keyType = byteToChar(buffer.readByte().toByte())
            2 -> valueType = byteToChar(buffer.readByte().toByte())
            3 -> defaultString = buffer.readString()
            4 -> defaultInt = buffer.readInt()
            5 -> {
                length = buffer.readShort()
                val hashtable = Int2ObjectOpenHashMap<Any>(length)
                for (count in 0 until length) {
                    val id = buffer.readInt()
                    hashtable[id] = buffer.readString()
                }
                map = hashtable
            }
            6 -> {
                length = buffer.readShort()
                val hashtable = Int2IntOpenHashMap(length)
                for (count in 0 until length) {
                    val id = buffer.readInt()
                    hashtable[id] = buffer.readInt()
                }
                map = hashtable
            }
        }
    }
}
