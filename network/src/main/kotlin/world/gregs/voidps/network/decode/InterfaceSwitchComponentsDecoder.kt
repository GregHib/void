package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.MoveContainerItem
import world.gregs.voidps.network.misc.Interface
import world.gregs.voidps.network.readShortAddLittle
import world.gregs.voidps.network.readUnsignedIntMiddle

class InterfaceSwitchComponentsDecoder : Decoder(16) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val fromPacked = packet.readInt()
        val toSlot = packet.readShortLittleEndian().toInt()
        val toPacked = packet.readUnsignedIntMiddle()
        val fromItemId = packet.readShort().toInt()
        val fromSlot = packet.readShortAddLittle()
        val toItemId = packet.readShortAddLittle()
        instructions.emit(MoveContainerItem(
            fromId = Interface.getId(fromPacked),
            fromComponentId = Interface.getComponentId(fromPacked),
            fromType = fromItemId,
            fromSlot = fromSlot,
            toId = Interface.getId(toPacked),
            toComponentId = Interface.getComponentId(toPacked),
            toType = toItemId,
            toSlot = toSlot
        ))
    }

}