package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Configs.CONTAINERS
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.ContainerDefinition

class ContainerDecoder : ConfigDecoder<ContainerDefinition>(CONTAINERS) {

    override fun create() = ContainerDefinition()

    override fun create(size: Int): Array<ContainerDefinition> {
        return Array(size) { ContainerDefinition(it) }
    }

    override fun ContainerDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            2 -> length = buffer.readUnsignedShort()
            4 -> {
                val size = buffer.readUnsignedByte()
                ids = IntArray(size)
                amounts = IntArray(size)
                for (i in 0 until size) {
                    ids!![i] = buffer.readUnsignedShort()
                    amounts!![i] = buffer.readUnsignedShort()
                }
            }
        }
    }
}