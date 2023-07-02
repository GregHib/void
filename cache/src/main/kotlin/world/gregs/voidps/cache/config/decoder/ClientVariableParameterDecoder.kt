package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Configs.VARC
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.ClientVariableParameterDefinition

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