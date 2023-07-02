package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices.VAR_BIT
import world.gregs.voidps.cache.definition.data.VarBitDefinition

class VarBitDecoder : DefinitionDecoder<VarBitDefinition>(VAR_BIT) {

    override fun size(cache: Cache): Int {
        return cache.lastArchiveId(index) * 0x400 + cache.archiveCount(index, cache.lastArchiveId(index))
    }

    override fun create() = VarBitDefinition()

    override fun getFile(id: Int) = id and 0x3ff

    override fun getArchive(id: Int) = id ushr 10

    override fun VarBitDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 1) {
            index = buffer.readShort()
            startBit = buffer.readUnsignedByte()
            endBit = buffer.readUnsignedByte()
        }
    }
}