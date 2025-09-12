package world.gregs.voidps.network.login.protocol.decode

import kotlinx.io.Source
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.login.protocol.Decoder

class InterfaceOptionDecoder(private val index: Int) : Decoder(8) {

    override suspend fun decode(packet: Source): Instruction {
        val packed = packet.readInt()
        return InteractInterface(
            interfaceId = InterfaceDefinition.id(packed),
            componentId = InterfaceDefinition.componentId(packed),
            itemId = packet.readShort().toInt(),
            itemSlot = packet.readShort().toInt(),
            option = index,
        )
    }
}
