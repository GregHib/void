package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractObject
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBooleanAdd
import world.gregs.voidps.network.login.protocol.readUnsignedShortAdd

class ObjectOption4Decoder : Decoder(7) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val run = packet.readBooleanAdd()
        val objectId = packet.readUnsignedShortAdd()
        val x = packet.readUnsignedShortAdd()
        val y = packet.readShortLittleEndian().toInt()
        return InteractObject(objectId, x, y, 4)
    }
}
