package world.gregs.void.cache.config.decoder

import world.gregs.void.buffer.read.Reader
import world.gregs.void.cache.Cache
import world.gregs.void.cache.Configs.VARC
import world.gregs.void.cache.config.ConfigDecoder
import world.gregs.void.cache.config.data.ClientVariableParameterDefinition

/**
 * @author GregHib <greg@gregs.world>
 * @since April 08, 2020
 */
class ClientVariableParameterDecoder(cache: Cache) : ConfigDecoder<ClientVariableParameterDefinition>(cache, VARC) {

    override fun create() = ClientVariableParameterDefinition()

    override fun ClientVariableParameterDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 1) {
            aChar3210 = byteToChar(buffer.readByte().toByte())
        } else if (opcode == 2) {
            anInt3208 = 0
        }
    }
}