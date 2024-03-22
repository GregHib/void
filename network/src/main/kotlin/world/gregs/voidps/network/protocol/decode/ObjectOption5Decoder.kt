package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.protocol.readBooleanAdd
import world.gregs.voidps.network.protocol.readShortAddLittle
import world.gregs.voidps.network.protocol.readUnsignedShortAdd

class ObjectOption5Decoder : Decoder(7) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val y = packet.readShortLittleEndian().toInt()
        val run = packet.readBooleanAdd()
        val x = packet.readShortAddLittle()
        val objectId = packet.readUnsignedShortAdd() and 0xffff
        instructions.emit(InteractObject(objectId, x, y, 5))
    }

}