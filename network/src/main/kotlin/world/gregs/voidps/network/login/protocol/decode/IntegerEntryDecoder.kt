package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.EnterInt
import world.gregs.voidps.network.login.protocol.Decoder

class IntegerEntryDecoder : Decoder(4) {

    override suspend fun decode(packet: Source): Instruction {
        val integer = packet.readInt()
        return EnterInt(integer)
    }
}
