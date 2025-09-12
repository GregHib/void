package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.io.Source
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.MoveInventoryItem
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readShortAddLittle
import world.gregs.voidps.network.login.protocol.readUnsignedIntMiddle

class InterfaceSwitchComponentsDecoder : Decoder(16) {

    override suspend fun decode(packet: Source): Instruction {
        val fromPacked = packet.readInt()
        val toSlot = packet.readShortLittleEndian().toInt()
        val toPacked = packet.readUnsignedIntMiddle()
        val toItemId = packet.readShort().toInt()
        val fromSlot = packet.readShortAddLittle()
        val fromItemId = packet.readShortAddLittle()
        return MoveInventoryItem(
            fromId = InterfaceDefinition.id(fromPacked),
            fromComponentId = InterfaceDefinition.componentId(fromPacked),
            fromItemId = fromItemId,
            fromSlot = fromSlot,
            toId = InterfaceDefinition.id(toPacked),
            toComponentId = InterfaceDefinition.componentId(toPacked),
            toItemId = toItemId,
            toSlot = toSlot,
        )
    }
}
