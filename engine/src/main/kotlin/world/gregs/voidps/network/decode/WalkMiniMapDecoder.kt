package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.network.readBooleanAdd
import world.gregs.voidps.network.readUnsignedShortAdd

class WalkMiniMapDecoder : Decoder(18) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val y = packet.readShortLittleEndian().toInt()
        val running = packet.readBooleanAdd()
        val x = packet.readUnsignedShortAdd()
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
        instructions.emit(Walk(x, y))
    }

}