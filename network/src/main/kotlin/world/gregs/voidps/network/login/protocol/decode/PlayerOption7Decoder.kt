package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractPlayer
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readByteAdd
import world.gregs.voidps.network.login.protocol.readUnsignedShortAdd

class PlayerOption7Decoder : Decoder(3) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val index = packet.readUnsignedShortAdd()
        packet.readByteAdd()
        return InteractPlayer(index, 7)
    }
}
