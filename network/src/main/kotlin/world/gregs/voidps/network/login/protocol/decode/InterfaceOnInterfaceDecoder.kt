package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractInterfaceItem
import world.gregs.voidps.network.login.protocol.*

class InterfaceOnInterfaceDecoder : Decoder(16) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val fromPacked = packet.g4Alt2()
        val fromItem = packet.g2Alt2()
        val fromSlot = packet.g2Alt3()
        val toPacked = packet.g4Alt3()
        val toItem = packet.g2Alt2()
        val toSlot = packet.g2Alt1()
        return InteractInterfaceItem(
            fromItem,
            toItem,
            fromSlot,
            toSlot,
            InterfaceDefinition.id(fromPacked),
            InterfaceDefinition.componentId(fromPacked),
            InterfaceDefinition.id(toPacked),
            InterfaceDefinition.componentId(toPacked)
        )
    }

}