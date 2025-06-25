package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.MoveInventoryItem
import world.gregs.voidps.network.login.protocol.*

class InterfaceSwitchComponentsDecoder : Decoder(16) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val fromItemId = packet.readShort().toInt()
        val fromSlot = packet.g2Alt1()
        val toItemId = packet.g2Alt2()
        val fromPacked = packet.g4Alt2()
        val toSlot = packet.g2Alt1()
        val toPacked = packet.g4Alt3()
        return MoveInventoryItem(
            fromId = InterfaceDefinition.id(fromPacked),
            fromComponentId = InterfaceDefinition.componentId(fromPacked),
            fromItemId = fromItemId,
            fromSlot = fromSlot,
            toId = InterfaceDefinition.id(toPacked),
            toComponentId = InterfaceDefinition.componentId(toPacked),
            toItemId = toItemId,
            toSlot = toSlot
        )
    }

}