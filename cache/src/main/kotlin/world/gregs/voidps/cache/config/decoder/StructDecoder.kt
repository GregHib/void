package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Configs.STRUCTS
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.StructDefinition

class StructDecoder : ConfigDecoder<StructDefinition>(STRUCTS) {

    override fun create() = StructDefinition()

    override fun create(size: Int): Array<StructDefinition> {
        return Array(size) { StructDefinition(it) }
    }

    override fun StructDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 249) {
            readParameters(buffer)
        }
    }
}