package rs.dusk.cache.config.decoder

import rs.dusk.cache.Cache
import rs.dusk.cache.Configs.VARP
import rs.dusk.cache.config.ConfigDecoder
import rs.dusk.cache.config.data.PlayerVariableParameterDefinition
import rs.dusk.core.io.read.Reader

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