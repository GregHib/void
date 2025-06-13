package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractInterfaceNPC
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBooleanAdd
import world.gregs.voidps.network.login.protocol.readShortAddLittle
import world.gregs.voidps.network.login.protocol.readUnsignedShortAdd

class InterfaceOnNpcDecoder : Decoder(11) {

    override suspend fun decode(packet: ByteReadPacket): Instruction {
        val slot = packet.readShortAddLittle()
        val packed = packet.readInt()
        val npc = packet.readShortLittleEndian().toInt()
        val run = packet.readBooleanAdd()
        val itemId = packet.readUnsignedShortAdd()
        return InteractInterfaceNPC(
            npc,
            InterfaceDefinition.id(packed),
            InterfaceDefinition.componentId(packed),
            itemId,
            slot,
        )
    }
}
