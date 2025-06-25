package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractInterfacePlayer
import world.gregs.voidps.network.login.protocol.*

class InterfaceOnPlayerDecoder : Decoder(11) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val index = packet.g2Alt1()
        val packed = packet.g4Alt1()
        val itemId = packet.readShort().toInt()
        val run = packet.g1Alt3() == 1
        val slot = packet.g2Alt3()
        return InteractInterfacePlayer(
            index,
            InterfaceDefinition.id(packed),
            InterfaceDefinition.componentId(packed),
            itemId,
            slot
        )
    }

}