package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBooleanInverse
import world.gregs.voidps.network.login.protocol.readShortAdd
import world.gregs.voidps.network.login.protocol.readUnsignedShortAdd

class FloorItemOption2Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val y = packet.readUnsignedShortAdd()
        val id = packet.readShortAdd()
        val x = packet.readShortLittleEndian().toInt()
        val run = packet.readBooleanInverse()
        instructions.emit(InteractFloorItem(id, x, y, 1))
    }

}