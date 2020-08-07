package rs.dusk.cache.definition.decoder

import rs.dusk.cache.Cache
import rs.dusk.cache.DefinitionDecoder
import rs.dusk.cache.Indices.VAR_BIT
import rs.dusk.cache.definition.data.VarBitDefinition
import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class VarBitDecoder(cache: Cache) : DefinitionDecoder<VarBitDefinition>(cache, VAR_BIT) {

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