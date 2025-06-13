package world.gregs.voidps.cache.definition.decoder

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.DEFAULTS
import world.gregs.voidps.cache.definition.data.BodyDefinition

class BodyDecoder : DefinitionDecoder<BodyDefinition>(DEFAULTS) {

    val logger = InlineLogger()
    var definition: BodyDefinition? = null

    override fun create(size: Int) = Array(size) { BodyDefinition(it) }

    override fun getFile(id: Int) = 0

    override fun getArchive(id: Int) = 6

    override fun BodyDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> disabledSlots = IntArray(buffer.readUnsignedByte()) { buffer.readUnsignedByte() }
            3 -> anInt4506 = buffer.readUnsignedByte()
            4 -> anInt4504 = buffer.readUnsignedByte()
            5 -> anIntArray4501 = IntArray(buffer.readUnsignedByte()) { buffer.readUnsignedByte() }
            6 -> anIntArray4507 = IntArray(buffer.readUnsignedByte()) { buffer.readUnsignedByte() }
        }
    }
}
