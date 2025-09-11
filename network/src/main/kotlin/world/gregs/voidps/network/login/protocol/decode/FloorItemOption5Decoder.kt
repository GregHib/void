package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractFloorItem
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBooleanInverse
import world.gregs.voidps.network.login.protocol.readShortAdd

class FloorItemOption5Decoder : Decoder(7) {

    override suspend fun decode(packet: Source): Instruction {
        val y = packet.readShort().toInt()
        val x = packet.readShortAdd()
        val run = packet.readBooleanInverse()
        val id = packet.readShortAdd()
        return InteractFloorItem(id, x, y, 4)
    }
}
