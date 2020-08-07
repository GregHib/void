package rs.dusk.cache.definition.decoder

import com.github.michaelbull.logging.InlineLogger
import rs.dusk.cache.Cache
import rs.dusk.cache.DefinitionDecoder
import rs.dusk.cache.Indices.DEFAULTS
import rs.dusk.cache.definition.data.BodyDefinition
import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class BodyDecoder(cache: Cache) : DefinitionDecoder<BodyDefinition>(cache, DEFAULTS) {

    val logger = InlineLogger()

    override fun create() = BodyDefinition()

    override fun getFile(id: Int) = 0

    override fun getArchive(id: Int) = 6

    init {
        val definition = super.readData(0)
        if (definition != null) {
            dataCache[0] = definition
        } else {
            logger.info { "Unable to find body definitions" }
        }
    }

    override fun readData(id: Int) = dataCache[id]

    override fun BodyDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> {
                val length = buffer.readUnsignedByte()
                disabledSlots = IntArray(length)
                repeat(disabledSlots.size) { count ->
                    disabledSlots[count] = buffer.readUnsignedByte()
                }
            }
            3 -> anInt4506 = buffer.readUnsignedByte()
            4 -> anInt4504 = buffer.readUnsignedByte()
            5 -> {
                anIntArray4501 = IntArray(buffer.readUnsignedByte())
                repeat(anIntArray4501!!.size) { count ->
                    anIntArray4501!![count] = buffer.readUnsignedByte()
                }
            }
            6 -> {
                anIntArray4507 = IntArray(buffer.readUnsignedByte())
                repeat(anIntArray4507!!.size) { count ->
                    anIntArray4507!![count] = buffer.readUnsignedByte()
                }
            }
        }
    }
}