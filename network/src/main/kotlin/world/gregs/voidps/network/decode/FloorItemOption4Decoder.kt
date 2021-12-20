package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.InteractFloorItem
import world.gregs.voidps.network.readBooleanSubtract
import world.gregs.voidps.network.readUnsignedShortAdd

class FloorItemOption4Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val run = packet.readBooleanSubtract()
        val x = packet.readUnsignedShortAdd()
        val y = packet.readShortLittleEndian().toInt()
        val id = packet.readShort().toInt()
        instructions.emit(InteractFloorItem(id, x, y, 3))
    }

}