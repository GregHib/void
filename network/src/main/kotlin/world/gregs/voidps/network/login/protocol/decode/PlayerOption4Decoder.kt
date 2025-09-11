package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractPlayer
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readByteAdd

class PlayerOption4Decoder : Decoder(3) {

    override suspend fun decode(packet: Source): Instruction {
        val index = packet.readShort().toInt()
        packet.readByteAdd()
        return InteractPlayer(index, 4)
    }
}
