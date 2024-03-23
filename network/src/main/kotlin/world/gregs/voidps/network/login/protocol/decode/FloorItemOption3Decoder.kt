package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBoolean
import world.gregs.voidps.network.login.protocol.readUnsignedShortAdd
import world.gregs.voidps.network.login.protocol.readUnsignedShortAddLittle

class FloorItemOption3Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val id = packet.readShort().toInt()
        val x = packet.readUnsignedShortAdd()
        val run = packet.readBoolean()
        val y = packet.readUnsignedShortAddLittle()
        instructions.emit(InteractFloorItem(id, x, y, 2))
    }

}