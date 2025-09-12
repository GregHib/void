package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractPlayer
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readUnsignedShortAdd

class PlayerOption5Decoder : Decoder(3) {

    override suspend fun decode(packet: Source): Instruction {
        val index = packet.readUnsignedShortAdd()
        packet.readByte()
        return InteractPlayer(index, 5)
    }
}
