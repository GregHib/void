package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractPlayer
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readUnsignedShortAdd

class PlayerOption5Decoder : Decoder(3) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        packet.readByte()
        val index = packet.readShort().toInt()
        return InteractPlayer(index, 5)
    }

}