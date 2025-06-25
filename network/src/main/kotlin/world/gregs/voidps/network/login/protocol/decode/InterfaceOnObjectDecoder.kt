package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractInterfaceObject
import world.gregs.voidps.network.login.protocol.*

class InterfaceOnObjectDecoder : Decoder(15) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val run = packet.g1Alt2() == 1
        val y = packet.g2Alt1()
        val index = packet.g2Alt1()
        val packed = packet.g4Alt1()
        val item = packet.g2Alt3()
        val x = packet.g2Alt1()
        val objectId = packet.g2Alt2()
        return InteractInterfaceObject(
            objectId,
            x,
            y,
            InterfaceDefinition.id(packed),
            InterfaceDefinition.componentId(packed),
            item,
            index
        )
    }

}