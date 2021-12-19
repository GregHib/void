package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.InteractInterfaceItem
import world.gregs.voidps.network.misc.Interface
import world.gregs.voidps.network.readShortAdd
import world.gregs.voidps.network.readUnsignedIntInverseMiddle

class InterfaceOnInterfaceDecoder : Decoder(16) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val fromHash = packet.readInt()
        val toHash = packet.readUnsignedIntInverseMiddle()
        val fromItem = packet.readShortAdd()
        val from = packet.readShort().toInt()
        val toItem = packet.readShortAdd()
        val to = packet.readShort().toInt()
        instructions.emit(
            InteractInterfaceItem(
                from,
                to,
                fromItem,
                toItem,
                Interface.getId(fromHash),
                Interface.getComponentId(fromHash),
                Interface.getId(toHash),
                Interface.getComponentId(toHash)
            )
        )
    }

}