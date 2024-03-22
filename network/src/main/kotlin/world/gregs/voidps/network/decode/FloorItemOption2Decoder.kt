package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.readBooleanInverse
import world.gregs.voidps.network.readShortAdd

class FloorItemOption2Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val y = packet.readShortAdd()
        val id = packet.readShortAdd()
        val x = packet.readShortLittleEndian().toInt()
        val run = packet.readBooleanInverse()
        instructions.emit(InteractFloorItem(id, x, y, 1))
    }

}