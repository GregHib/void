package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractDialogueItem
import world.gregs.voidps.network.login.protocol.Decoder

class DialogueContinueItemDecoder : Decoder(2) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val id = packet.readShort().toInt()
        return InteractDialogueItem(id)
    }
}
