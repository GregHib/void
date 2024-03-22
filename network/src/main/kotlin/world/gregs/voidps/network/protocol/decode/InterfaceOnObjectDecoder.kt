package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractInterfaceObject
import world.gregs.voidps.network.protocol.*

class InterfaceOnObjectDecoder : Decoder(15) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val item = packet.readShort().toInt()
        val x = packet.readShortAddLittle()
        val packed = packet.readIntLittleEndian()
        val y = packet.readUnsignedShortAdd()
        val run = packet.readBooleanSubtract()
        val index = packet.readShortLittleEndian().toInt()
        val objectId = packet.readUnsignedShortLittle()
        instructions.emit(InteractInterfaceObject(
            objectId,
            x,
            y,
            InterfaceDefinition.id(packed),
            InterfaceDefinition.componentId(packed),
            item,
            index
        ))
    }

}