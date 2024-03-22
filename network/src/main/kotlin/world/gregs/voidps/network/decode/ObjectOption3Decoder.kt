package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.network.readBooleanAdd
import world.gregs.voidps.network.readUnsignedShortAdd
import world.gregs.voidps.network.readUnsignedShortAddLittle

class ObjectOption3Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val y = packet.readUnsignedShortAdd()
        val objectId = packet.readUnsignedShortAddLittle()
        val x = packet.readShortLittleEndian().toInt()
        val run = packet.readBooleanAdd()
        instructions.emit(InteractObject(objectId, x, y, 3))
    }

}