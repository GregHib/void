package world.gregs.void.cache.definition.decoder

import world.gregs.void.buffer.read.Reader
import world.gregs.void.cache.DefinitionDecoder
import world.gregs.void.cache.Indices.VAR_BIT
import world.gregs.void.cache.definition.data.VarBitDefinition

/**
 * @author GregHib <greg@gregs.world>
 * @since April 08, 2020
 */
class VarBitDecoder(cache: world.gregs.void.cache.Cache) : DefinitionDecoder<VarBitDefinition>(cache, VAR_BIT) {

    override fun create() = VarBitDefinition()

    override fun getFile(id: Int) = id and 0x3ff

    override fun getArchive(id: Int) = id ushr 10

    override fun VarBitDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 1) {
            index = buffer.readShort()
            leastSignificantBit = buffer.readUnsignedByte()
            mostSignificantBit = buffer.readUnsignedByte()
        }
    }
}