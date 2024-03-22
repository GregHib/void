package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.protocol.readBooleanAdd
import world.gregs.voidps.network.protocol.readUnsignedShortAdd

class ObjectOption4Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val run = packet.readBooleanAdd()
        val objectId = packet.readUnsignedShortAdd()
        val x = packet.readUnsignedShortAdd()
        val y = packet.readShortLittleEndian().toInt()
        instructions.emit(InteractObject(objectId, x, y, 4))
    }

}