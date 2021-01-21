package world.gregs.void.cache.config.decoder

import world.gregs.void.buffer.read.Reader
import world.gregs.void.cache.Configs.STRUCTS
import world.gregs.void.cache.config.ConfigDecoder
import world.gregs.void.cache.config.data.StructDefinition

/**
 * @author GregHib <greg@gregs.world>
 * @since April 08, 2020
 */
class StructDecoder(cache: world.gregs.void.cache.Cache) : ConfigDecoder<StructDefinition>(cache, STRUCTS) {

    override fun create() = StructDefinition()

    override fun StructDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 249) {
            readParameters(buffer)
        }
    }
}