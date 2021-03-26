package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.engine.client.ui.Interface
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.MoveContainerItem
import world.gregs.voidps.network.readShortAddLittle
import world.gregs.voidps.network.readUnsignedIntMiddle

class InterfaceSwitchComponentsDecoder : Decoder(16) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val fromPacked = packet.readInt()
        val toSlot = packet.readShortLittleEndian().toInt()
        val toPacked = packet.readUnsignedIntMiddle()
        val fromType = packet.readShort().toInt()
        val fromSlot = packet.readShortAddLittle()
        val toType = packet.readShortAddLittle()
        instructions.emit(MoveContainerItem(
            fromId = Interface.getId(fromPacked),
            fromComponentId = Interface.getComponentId(fromPacked),
            fromType = fromType,
            fromSlot = fromSlot,
            toId = Interface.getId(toPacked),
            toComponentId = Interface.getComponentId(toPacked),
            toType = toType,
            toSlot = toSlot
        ))
    }

}