package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.protocol.readBooleanSubtract
import world.gregs.voidps.network.protocol.readShortAddLittle

class ObjectOption1Decoder : Decoder(7) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val run = packet.readBooleanSubtract()
        val x = packet.readShortAddLittle()
        val y = packet.readUShort().reverseByteOrder().toInt()
        val objectId = packet.readUShort().toInt()
        instructions.emit(InteractObject(objectId, x, y, 1))
    }

}