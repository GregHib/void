package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Config.VARP
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.PlayerVariableParameterDefinition

class PlayerVariableParameterDecoder : ConfigDecoder<PlayerVariableParameterDefinition>(VARP) {

    override fun create(size: Int) = Array(size) { PlayerVariableParameterDefinition(it) }

    override fun PlayerVariableParameterDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 5) {
            type = buffer.readShort()
        }
    }
}
