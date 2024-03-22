package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.readBoolean
import world.gregs.voidps.network.readUnsignedShortAdd
import world.gregs.voidps.network.readUnsignedShortAddLittle

class FloorItemOption3Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val id = packet.readShort().toInt()
        val x = packet.readUnsignedShortAdd()
        val run = packet.readBoolean()
        val y = packet.readUnsignedShortAddLittle()
        instructions.emit(InteractFloorItem(id, x, y, 2))
    }

}