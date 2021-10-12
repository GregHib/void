package world.gregs.voidps.network.decode

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.InteractObject
import world.gregs.voidps.network.readBooleanSubtract
import world.gregs.voidps.network.readShortAddLittle

class ObjectOption1Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val run = packet.readBooleanSubtract()
        val x = packet.readShortAddLittle()
        val y = packet.readUShort().reverseByteOrder().toInt()
        val objectId = packet.readUShort().toInt()
        instructions.emit(InteractObject(objectId, x, y, 1))
    }

}