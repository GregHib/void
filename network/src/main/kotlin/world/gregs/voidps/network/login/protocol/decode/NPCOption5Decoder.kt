package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractNPC
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBooleanAdd

class NPCOption5Decoder : Decoder(3) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val run = packet.readBooleanAdd()
        val npcIndex = packet.readShortLittleEndian().toInt()
        return InteractNPC(npcIndex, 5)
    }

}