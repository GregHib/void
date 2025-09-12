package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractInterfaceItem
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readShortAdd
import world.gregs.voidps.network.login.protocol.readUnsignedIntInverseMiddle

class InterfaceOnInterfaceDecoder : Decoder(16) {

    override suspend fun decode(packet: Source): Instruction {
        val toPacked = packet.readInt()
        val fromPacked = packet.readUnsignedIntInverseMiddle()
        val fromSlot = packet.readShortAdd()
        val fromItem = packet.readShort().toInt()
        val toSlot = packet.readShortAdd()
        val toItem = packet.readShort().toInt()
        return InteractInterfaceItem(
            fromItem,
            toItem,
            fromSlot,
            toSlot,
            InterfaceDefinition.id(fromPacked),
            InterfaceDefinition.componentId(fromPacked),
            InterfaceDefinition.id(toPacked),
            InterfaceDefinition.componentId(toPacked),
        )
    }
}
