package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractInterfaceNPC
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.protocol.readBooleanAdd
import world.gregs.voidps.network.protocol.readShortAddLittle
import world.gregs.voidps.network.protocol.readUnsignedShortAdd

class InterfaceOnNpcDecoder : Decoder(11) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val slot = packet.readShortAddLittle()
        val packed = packet.readInt()
        val npc = packet.readShortLittleEndian().toInt()
        val run = packet.readBooleanAdd()
        val itemId = packet.readUnsignedShortAdd()
        instructions.emit(InteractInterfaceNPC(
            npc,
            InterfaceDefinition.id(packed),
            InterfaceDefinition.componentId(packed),
            itemId,
            slot
        ))
    }

}