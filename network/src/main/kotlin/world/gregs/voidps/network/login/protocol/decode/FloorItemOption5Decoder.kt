package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBooleanInverse
import world.gregs.voidps.network.login.protocol.readShortAdd

class FloorItemOption5Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val y = packet.readShort().toInt()
        val x = packet.readShortAdd()
        val run = packet.readBooleanInverse()
        val id = packet.readShortAdd()
        instructions.emit(InteractFloorItem(id, x, y, 4))
    }

}