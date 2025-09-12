package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBooleanSubtract
import world.gregs.voidps.network.login.protocol.readUnsignedShortAdd

class FloorItemOption4Decoder : Decoder(7) {

    override suspend fun decode(packet: Source): Instruction {
        val run = packet.readBooleanSubtract()
        val x = packet.readUnsignedShortAdd()
        val y = packet.readShortLittleEndian().toInt()
        val id = packet.readShort().toInt()
        return InteractFloorItem(id, x, y, 3)
    }
}
