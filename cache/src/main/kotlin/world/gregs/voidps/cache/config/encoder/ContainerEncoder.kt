package world.gregs.voidps.cache.config.encoder

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.config.ConfigEncoder
import world.gregs.voidps.cache.config.data.ContainerDefinition

class ContainerEncoder : ConfigEncoder<ContainerDefinition>() {

    override fun Writer.encode(definition: ContainerDefinition) {
        if (definition.length != 0) {
            writeByte(2)
            writeShort(definition.length)
        }
        val ids = definition.ids
        val amounts = definition.amounts
        if (ids != null && amounts != null) {
            writeByte(4)
            writeByte(ids.size)
            repeat(ids.size) {
                writeShort(ids[it])
                writeShort(amounts[it])
            }
        }
        writeByte(0)
    }

}