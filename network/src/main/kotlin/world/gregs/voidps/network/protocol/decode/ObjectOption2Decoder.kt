package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.readBooleanSubtract
import world.gregs.voidps.network.readShortAddLittle
import world.gregs.voidps.network.readUnsignedShortAdd
import world.gregs.voidps.network.readUnsignedShortAddLittle

class ObjectOption2Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val y = packet.readShortAddLittle()
        val x = packet.readUnsignedShortAdd()
        val run = packet.readBooleanSubtract()
        val objectId = packet.readUnsignedShortAddLittle()
        instructions.emit(InteractObject(objectId, x, y, 2))
    }

}