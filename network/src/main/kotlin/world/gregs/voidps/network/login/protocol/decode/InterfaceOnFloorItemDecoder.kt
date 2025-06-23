package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractInterfaceFloorItem
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBoolean
import world.gregs.voidps.network.login.protocol.readUnsignedIntMiddle
import world.gregs.voidps.network.login.protocol.readUnsignedShortAdd

class InterfaceOnFloorItemDecoder : Decoder(15) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val x = packet.readShortLittleEndian().toInt()
        val floorItem = packet.readUnsignedShortAdd()
        val itemSlot = packet.readShortLittleEndian().toInt()
        val y = packet.readShort().toInt()
        val run = packet.readBoolean()
        val item = packet.readUnsignedShortAdd()
        val packed = packet.readUnsignedIntMiddle()
        return InteractInterfaceFloorItem(
            floorItem,
            x,
            y,
            InterfaceDefinition.id(packed),
            InterfaceDefinition.componentId(packed),
            item,
            itemSlot,
        )
    }
}
