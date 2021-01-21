package world.gregs.void.cache.definition.decoder

import world.gregs.void.buffer.read.Reader
import world.gregs.void.cache.DefinitionDecoder
import world.gregs.void.cache.Indices.ENUMS
import world.gregs.void.cache.definition.data.EnumDefinition

/**
 * @author GregHib <greg@gregs.world>
 * @since April 08, 2020
 */
class EnumDecoder(cache: world.gregs.void.cache.Cache) : DefinitionDecoder<EnumDefinition>(cache, ENUMS) {

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
                val hashtable = HashMap<Int, Any>()
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
                val strings = HashMap<Int, Any>(size)
                repeat(length) {
                    val index = buffer.readShort()
                    strings[index] = buffer.readString()
                }
                map = strings
            }
            8 -> {
                val size = buffer.readShort()
                length = buffer.readShort()
                val integers = HashMap<Int, Any>(size)
                repeat(length) {
                    val index = buffer.readShort()
                    integers[index] = buffer.readInt()
                }
                map = integers
            }
        }
    }
}