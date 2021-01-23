package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Configs.VARP
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.PlayerVariableParameterDefinition

/**
 * @author GregHib <greg@gregs.world>
 * @since April 08, 2020
 */
class PlayerVariableParameterDecoder(cache: Cache) : ConfigDecoder<PlayerVariableParameterDefinition>(cache, VARP) {

    override fun create() = PlayerVariableParameterDefinition()

    override fun PlayerVariableParameterDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 5) {
            type = buffer.readShort()
        }
    }
}