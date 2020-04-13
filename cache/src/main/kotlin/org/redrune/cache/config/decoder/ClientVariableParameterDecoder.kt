package org.redrune.cache.config.decoder

import org.redrune.cache.Configs.VARC
import org.redrune.cache.config.ConfigDecoder
import org.redrune.cache.config.data.ClientVariableParameterDefinition
import org.redrune.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class ClientVariableParameterDecoder : ConfigDecoder<ClientVariableParameterDefinition>(VARC) {

    override fun create() = ClientVariableParameterDefinition()

    override fun ClientVariableParameterDefinition.read(opcode: Int, buffer: Reader) {
        if (opcode == 1) {
            aChar3210 = byteToChar(buffer.readByte().toByte())
        } else if (opcode == 2) {
            anInt3208 = 0
        }
    }
}