package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractDialogue
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readShortAdd
import world.gregs.voidps.network.login.protocol.readUnsignedIntMiddle

class DialogueContinueDecoder : Decoder(6) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val button = packet.readShortAdd()
        val packed = packet.readUnsignedIntMiddle()
        return InteractDialogue(InterfaceDefinition.id(packed), InterfaceDefinition.componentId(packed), button)
    }
}
