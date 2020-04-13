package org.redrune.cache.definition.decoder

import org.redrune.cache.DefinitionDecoder
import org.redrune.cache.Indices.VAR_BIT
import org.redrune.cache.definition.data.VarBitDefinition
import org.redrune.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class VarBitDecoder : DefinitionDecoder<VarBitDefinition>(VAR_BIT) {

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