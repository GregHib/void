package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.*
import world.gregs.voidps.network.instruct.InteractObject

class ObjectOption3Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val y = packet.readUnsignedShortAdd()
        val objectId = packet.readUnsignedShortAddLittle()
        val x = packet.readShortLittleEndian().toInt()
        val run = packet.readBooleanAdd()
        instructions.emit(InteractObject(objectId, x, y, 3))
    }

}