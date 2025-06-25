package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractNPC
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.g1Alt1
import world.gregs.voidps.network.login.protocol.g2Alt2
import world.gregs.voidps.network.login.protocol.readBooleanAdd

class NPCOption4Decoder : Decoder(3) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val run = packet.g1Alt1() == 1
        val npcIndex = packet.g2Alt2()
        return InteractNPC(npcIndex, 4)
    }

}