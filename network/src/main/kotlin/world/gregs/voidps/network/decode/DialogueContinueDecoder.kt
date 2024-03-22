package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractDialogue
import world.gregs.voidps.network.readShortAdd
import world.gregs.voidps.network.readUnsignedIntMiddle

class DialogueContinueDecoder : Decoder(6) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val button = packet.readShortAdd()
        val packed = packet.readUnsignedIntMiddle()
        instructions.emit(InteractDialogue(InterfaceDefinition.id(packed), InterfaceDefinition.componentId(packed), button))
    }

}