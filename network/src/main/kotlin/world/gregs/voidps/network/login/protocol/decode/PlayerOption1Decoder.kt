package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractPlayer
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readByteInverse
import world.gregs.voidps.network.login.protocol.readUnsignedShortAddLittle

class PlayerOption1Decoder : Decoder(3) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val index = packet.readUnsignedShortAddLittle()
        packet.readByteInverse()
        return InteractPlayer(index, 1)
    }
}
