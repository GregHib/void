package rs.dusk.cache.config.decoder

import rs.dusk.buffer.read.Reader
import rs.dusk.cache.Cache
import rs.dusk.cache.Configs.CONTAINERS
import rs.dusk.cache.config.ConfigDecoder
import rs.dusk.cache.config.data.ContainerDefinition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class ContainerDecoder(cache: Cache) : ConfigDecoder<ContainerDefinition>(cache, CONTAINERS) {

    override fun create() = ContainerDefinition()

    override fun ContainerDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            2 -> length = buffer.readUnsignedShort()
            4 -> {
                val size = buffer.readUnsignedByte()
                ids = IntArray(size)
                amounts = IntArray(size)
                repeat(size) { i ->
                    ids!![i] = buffer.readUnsignedShort()
                    amounts!![i] = buffer.readUnsignedShort()
                }
            }
        }
    }
}