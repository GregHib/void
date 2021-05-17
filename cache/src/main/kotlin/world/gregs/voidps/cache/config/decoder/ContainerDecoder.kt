package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Configs.CONTAINERS
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.ContainerDefinition

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