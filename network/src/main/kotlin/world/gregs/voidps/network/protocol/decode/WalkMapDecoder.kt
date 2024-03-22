package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.network.protocol.Decoder
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