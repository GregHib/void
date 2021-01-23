package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Configs.STRUCTS
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.StructDefinition

/**
 * @author GregHib <greg@gregs.world>
 * @since April 08, 2020
 */
class StructDecoder(cache: world.gregs.voidps.cache.Cache) : ConfigDecoder<StructDefinition>(cache, STRUCTS) {

    override fun create() = StructDefinition()

    override fun StructDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 249) {
            readParameters(buffer)
        }
    }
}