package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Config.STRUCTS
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.StructDefinition
import world.gregs.voidps.cache.definition.Parameters

class StructDecoder(
    private val parameters: Parameters = Parameters.EMPTY,
) : ConfigDecoder<StructDefinition>(STRUCTS) {

    override fun create(size: Int) = Array(size) { StructDefinition(it) }

    override fun StructDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 249) {
            readParameters(buffer, parameters)
        }
    }
}
