package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractNPC
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBoolean

class NPCOption1Decoder : Decoder(3) {

    override suspend fun decode(packet: Source): Instruction {
        val run = packet.readBoolean()
        val npcIndex = packet.readShortLittleEndian().toInt()
        return InteractNPC(npcIndex, 1)
    }
}
