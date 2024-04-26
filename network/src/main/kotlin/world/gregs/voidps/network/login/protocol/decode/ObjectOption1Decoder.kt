package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBooleanSubtract
import world.gregs.voidps.network.login.protocol.readShortAddLittle

class ObjectOption1Decoder : Decoder(7) {

    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val run = packet.readBooleanSubtract()
        val x = packet.readShortAddLittle()
        val y = packet.readUShort().reverseByteOrder().toInt()
        val objectId = packet.readUShort().toInt()
        return InteractObject(objectId, x, y, 1)
    }

}