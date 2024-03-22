package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.protocol.readBooleanAdd
import world.gregs.voidps.network.protocol.readUnsignedShortAdd
import world.gregs.voidps.network.protocol.readUnsignedShortAddLittle

class ObjectOption3Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val y = packet.readUnsignedShortAdd()
        val objectId = packet.readUnsignedShortAddLittle()
        val x = packet.readShortLittleEndian().toInt()
        val run = packet.readBooleanAdd()
        instructions.emit(InteractObject(objectId, x, y, 3))
    }

}