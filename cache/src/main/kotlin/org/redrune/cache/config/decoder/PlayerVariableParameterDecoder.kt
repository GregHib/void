package org.redrune.cache.config.decoder

import org.redrune.cache.Configs.VARP
import org.redrune.cache.config.ConfigDecoder
import org.redrune.cache.config.data.PlayerVariableParameterDefinition
import org.redrune.storage.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class PlayerVariableParameterDecoder : ConfigDecoder<PlayerVariableParameterDefinition>(VARP) {

    override fun create() = PlayerVariableParameterDefinition()

    override fun PlayerVariableParameterDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 5) {
            type = buffer.readShort()
        }
    }
}