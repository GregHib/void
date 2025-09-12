package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.io.Source
import world.gregs.voidps.cache.definition.data.InterfaceDefinition
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractInterfacePlayer
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readBooleanInverse
import world.gregs.voidps.network.login.protocol.readShortAddLittle
import world.gregs.voidps.network.login.protocol.readUnsignedIntInverseMiddle

class InterfaceOnPlayerDecoder : Decoder(11) {

    override suspend fun decode(packet: Source): Instruction {
        val slot = packet.readShortAddLittle()
        val index = packet.readShortLittleEndian().toInt()
        val itemId = packet.readShortLittleEndian().toInt()
        val packed = packet.readUnsignedIntInverseMiddle()
        val run = packet.readBooleanInverse()
        return InteractInterfacePlayer(
            index,
            InterfaceDefinition.id(packed),
            InterfaceDefinition.componentId(packed),
            itemId,
            slot,
        )
    }
}
