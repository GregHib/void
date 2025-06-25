package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.g2Alt3
import world.gregs.voidps.network.login.protocol.g4Alt3

class InterfaceOptionDecoder(private val index: Int) : Decoder(8) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val packed = packet.g4Alt3()
        val interfaceId = InterfaceDefinition.id(packed)
        val componentId = InterfaceDefinition.componentId(packed)
        val itemId = packet.g2Alt3()
        val slotId = packet.readShort().toInt()

        println("$interfaceId $componentId $itemId $slotId")

        return InteractInterface(
            interfaceId = InterfaceDefinition.id(packed),
            componentId = InterfaceDefinition.componentId(packed),
            itemId = itemId,
            slotId = slotId,
            option = index
        )
    }

}