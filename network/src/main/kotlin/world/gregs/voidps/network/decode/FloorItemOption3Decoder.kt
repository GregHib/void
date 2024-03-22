package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.*
import world.gregs.voidps.network.client.instruction.InteractFloorItem

class FloorItemOption3Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val id = packet.readShort().toInt()
        val x = packet.readUnsignedShortAdd()
        val run = packet.readBoolean()
        val y = packet.readUnsignedShortAddLittle()
        instructions.emit(InteractFloorItem(id, x, y, 2))
    }

}