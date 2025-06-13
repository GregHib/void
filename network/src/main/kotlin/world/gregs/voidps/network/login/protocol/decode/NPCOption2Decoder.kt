package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractNPC
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBooleanAdd
import world.gregs.voidps.network.login.protocol.readShortAddLittle

class NPCOption2Decoder : Decoder(3) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val npcIndex = packet.readShortAddLittle()
        val run = packet.readBooleanAdd()
        return InteractNPC(npcIndex, 2)
    }
}
