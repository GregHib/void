package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.Walk
import world.gregs.voidps.network.readBooleanAdd
import world.gregs.voidps.network.readUnsignedShortAdd

class WalkMapDecoder : Decoder(5) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val y = packet.readShortLittleEndian().toInt()
        val running = packet.readBooleanAdd()
        val x = packet.readUnsignedShortAdd()
        instructions.emit(Walk(x, y))
    }

}