package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractInterfaceNPC
import world.gregs.voidps.network.login.protocol.*

class InterfaceOnNpcDecoder : Decoder(11) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val slot = packet.g2Alt3()
        val itemId = packet.g2Alt1()
        val npc = packet.g2Alt1()
        val packed = packet.g4Alt3()
        val run = packet.readByte().toInt() == 1
        return InteractInterfaceNPC(
            npc,
            InterfaceDefinition.id(packed),
            InterfaceDefinition.componentId(packed),
            itemId,
            slot
        )
    }

}