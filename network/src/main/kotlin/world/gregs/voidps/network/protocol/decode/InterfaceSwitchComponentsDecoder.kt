package world.gregs.voidps.network.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.MoveInventoryItem
import world.gregs.voidps.network.readShortAddLittle
import world.gregs.voidps.network.readUnsignedIntMiddle

class InterfaceSwitchComponentsDecoder : Decoder(16) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val fromPacked = packet.readInt()
        val toSlot = packet.readShortLittleEndian().toInt()
        val toPacked = packet.readUnsignedIntMiddle()
        val toItemId = packet.readShort().toInt()
        val fromSlot = packet.readShortAddLittle()
        val fromItemId = packet.readShortAddLittle()
        instructions.emit(MoveInventoryItem(
            fromId = InterfaceDefinition.id(fromPacked),
            fromComponentId = InterfaceDefinition.componentId(fromPacked),
            fromItemId = fromItemId,
            fromSlot = fromSlot,
            toId = InterfaceDefinition.id(toPacked),
            toComponentId = InterfaceDefinition.componentId(toPacked),
            toItemId = toItemId,
            toSlot = toSlot
        ))
    }

}