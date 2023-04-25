package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.*
import world.gregs.voidps.network.instruct.InteractInterfaceFloorItem
import world.gregs.voidps.network.misc.Interface

class InterfaceOnFloorItemDecoder : Decoder(15) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val x = packet.readShortLittleEndian().toInt()
        val floorItem = packet.readUnsignedShortAdd()
        val itemSlot = packet.readShortLittleEndian().toInt()
        val y = packet.readShort().toInt()
        val run = packet.readBoolean()
        val item = packet.readUnsignedShortAdd()
        val packed = packet.readUnsignedIntMiddle()
        instructions.emit(InteractInterfaceFloorItem(floorItem, x, y, Interface.getId(packed), Interface.getComponentId(packed), item, itemSlot))
    }

}