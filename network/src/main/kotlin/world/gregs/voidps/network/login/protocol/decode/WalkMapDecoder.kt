package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readShortAddLittle

class WalkMapDecoder : Decoder(5) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val x = packet.readShortAddLittle()
        val y = packet.readShortAddLittle()
        val running = packet.readByte().toInt() == 1
        return Walk(x, y)
    }

}