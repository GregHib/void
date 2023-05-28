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
        writeByte(0)
    }

}