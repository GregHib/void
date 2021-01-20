package world.gregs.void.cache.config.decoder

import world.gregs.void.buffer.read.Reader
import world.gregs.void.cache.Cache
import world.gregs.void.cache.Configs.VARP
import world.gregs.void.cache.config.ConfigDecoder
import world.gregs.void.cache.config.data.PlayerVariableParameterDefinition

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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