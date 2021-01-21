package world.gregs.void.cache.config.decoder

import world.gregs.void.buffer.read.Reader
import world.gregs.void.cache.Cache
import world.gregs.void.cache.Configs.CONTAINERS
import world.gregs.void.cache.config.ConfigDecoder
import world.gregs.void.cache.config.data.ContainerDefinition

/**
 * @author GregHib <greg@gregs.world>
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