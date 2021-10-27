package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.*
import world.gregs.voidps.network.instruct.InteractInterfacePlayer
import world.gregs.voidps.network.misc.Interface

class InterfaceOnPlayerDecoder : Decoder(11) {

    override suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket) {
        val type = packet.readShortAddLittle()
        val index = packet.readShortLittleEndian().toInt()
        val slot = packet.readShortLittleEndian().toInt()
        val hash = packet.readUnsignedIntInverseMiddle()
        val run = packet.readBooleanInverse()
        instructions.emit(InteractInterfacePlayer(index, Interface.getId(hash), Interface.getComponentId(hash), type, slot))
    }

}