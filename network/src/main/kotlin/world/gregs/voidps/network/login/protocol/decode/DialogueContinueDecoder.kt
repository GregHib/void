package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractDialogue
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readIntInverseMiddle
import world.gregs.voidps.network.login.protocol.readIntV2
import world.gregs.voidps.network.login.protocol.readShortAddLittle

class DialogueContinueDecoder : Decoder(6) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val packed = packet.readIntV2()
        val button = packet.readShortAddLittle()
        return InteractDialogue(InterfaceDefinition.id(packed), InterfaceDefinition.componentId(packed), button)
    }

}