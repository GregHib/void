package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.protocol.Decoder
import world.gregs.voidps.network.protocol.readBooleanInverse
import world.gregs.voidps.network.protocol.readShortAddLittle
import world.gregs.voidps.network.protocol.readUnsignedIntInverseMiddle

class InterfaceOnPlayerDecoder : Decoder(11) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val slot = packet.readShortAddLittle()
        val index = packet.readShortLittleEndian().toInt()
        val itemId = packet.readShortLittleEndian().toInt()
        val packed = packet.readUnsignedIntInverseMiddle()
        val run = packet.readBooleanInverse()
        instructions.emit(world.gregs.voidps.network.client.instruction.InteractInterfacePlayer(
            index,
            InterfaceDefinition.id(packed),
            InterfaceDefinition.componentId(packed),
            itemId,
            slot
        ))
    }

}