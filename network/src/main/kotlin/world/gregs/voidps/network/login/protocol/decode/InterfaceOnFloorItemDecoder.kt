package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractInterfaceFloorItem
import world.gregs.voidps.network.login.protocol.*

class InterfaceOnFloorItemDecoder : Decoder(15) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val x = packet.readShort().toInt()
        val y = packet.readShort().toInt()
        val item = packet.g2Alt3()
        val packed = packet.g4Alt3()
        val itemSlot = packet.g2Alt1()
        val run = packet.readBoolean()
        val floorItem = packet.g2Alt1()
        return InteractInterfaceFloorItem(
            floorItem,
            x,
            y,
            InterfaceDefinition.id(packed),
            InterfaceDefinition.componentId(packed),
            item,
            itemSlot
        )
    }

}