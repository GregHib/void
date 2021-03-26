package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.engine.client.ui.Interface
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.InteractInterface

class InterfaceOptionDecoder(private val index: Int) : Decoder(8) {


    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val packed = packet.readInt()
        instructions.emit(InteractInterface(
            interfaceId = Interface.getId(packed),
            componentId = Interface.getComponentId(packed),
            itemId = packet.readShort().toInt(),
            itemSlot = packet.readShort().toInt(),
            option = index
        ))
    }

}