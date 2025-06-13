package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractPlayer
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readUnsignedShortAdd

class PlayerOption8Decoder : Decoder(3) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        packet.readByte()
        val index = packet.readUnsignedShortAdd()
        return InteractPlayer(index, 8)
    }
}
