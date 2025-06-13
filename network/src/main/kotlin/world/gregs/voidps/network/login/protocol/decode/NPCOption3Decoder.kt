package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractNPC
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBoolean

class NPCOption3Decoder : Decoder(3) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val npcIndex = packet.readShort().toInt()
        val run = packet.readBoolean()
        return InteractNPC(npcIndex, 3)
    }
}
