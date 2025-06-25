package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractNPC
import world.gregs.voidps.network.login.protocol.*

class NPCOption2Decoder : Decoder(3) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val run = packet.g1Alt1() == 1
        val npcIndex = packet.g2Alt2()
        return InteractNPC(npcIndex, 2)
    }

}