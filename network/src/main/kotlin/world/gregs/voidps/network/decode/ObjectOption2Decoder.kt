package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.*
import world.gregs.voidps.network.instruct.InteractObject

class ObjectOption2Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val y = packet.readShortAddLittle()
        val x = packet.readUnsignedShortAdd()
        val run = packet.readBooleanSubtract()
        val objectId = packet.readUnsignedShortAddLittle()
        instructions.emit(InteractObject(objectId, x, y, 2))
    }

}