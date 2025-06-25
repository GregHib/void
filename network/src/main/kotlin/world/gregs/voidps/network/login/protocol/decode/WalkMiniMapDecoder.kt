package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBooleanAdd
import world.gregs.voidps.network.login.protocol.readShortAddLittle
import world.gregs.voidps.network.login.protocol.readUnsignedShortAdd

class WalkMiniMapDecoder : Decoder(18) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val x = packet.readShortAddLittle()
        val y = packet.readShortAddLittle()
        val running = packet.readBooleanAdd()
        packet.readByte()//-1
        packet.readByte()//-1
        packet.readShort()//Rotation?
        packet.readByte()//57
        val minimapRotation = packet.readByte()
        val minimapZoom = packet.readByte()
        packet.readByte()//89
        packet.readShort()//X in region?
        packet.readShort()//Y in region?
        packet.readByte()//63
        return Walk(x, y, minimap = true)
    }

}