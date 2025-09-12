package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.InteractPlayer
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readByteSubtract

class PlayerOption3Decoder : Decoder(3) {

    override suspend fun decode(packet: Source): Instruction {
        packet.readByteSubtract()
        val index = packet.readShortLittleEndian().toInt()
        return InteractPlayer(index, 3)
    }
}
