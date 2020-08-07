package rs.dusk.cache.config.decoder

import rs.dusk.cache.Cache
import rs.dusk.cache.Configs.VARC
import rs.dusk.cache.config.ConfigDecoder
import rs.dusk.cache.config.data.ClientVariableParameterDefinition
import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
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