package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.readBooleanSubtract

class FloorItemOption1Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val id = packet.readShort().toInt()
        val x = packet.readShort().toInt()
        val y = packet.readShort().toInt()
        val run = packet.readBooleanSubtract()
        instructions.emit(InteractFloorItem(id, x, y, 0))
    }

}