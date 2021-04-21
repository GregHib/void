package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.engine.client.ui.Interface
import world.gregs.voidps.network.*
import world.gregs.voidps.network.instruct.InteractInterfaceNPC

class InterfaceOnNpcDecoder : Decoder(11) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val slot = packet.readShortAddLittle()
        val packed = packet.readInt()
        val npc = packet.readShortLittleEndian().toInt()
        val run = packet.readBooleanAdd()
        val type = packet.readShortAdd()
        instructions.emit(InteractInterfaceNPC(npc, Interface.getId(packed), Interface.getComponentId(packed), type, slot))
    }

}