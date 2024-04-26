package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractInterfaceObject
import world.gregs.voidps.network.login.protocol.*

class InterfaceOnObjectDecoder : Decoder(15) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val item = packet.readShort().toInt()
        val x = packet.readShortAddLittle()
        val packed = packet.readIntLittleEndian()
        val y = packet.readUnsignedShortAdd()
        val run = packet.readBooleanSubtract()
        val index = packet.readShortLittleEndian().toInt()
        val objectId = packet.readUnsignedShortLittle()
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