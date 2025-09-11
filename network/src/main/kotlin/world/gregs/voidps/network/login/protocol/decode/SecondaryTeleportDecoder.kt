package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import kotlinx.io.Source
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readShortAddLittle

class SecondaryTeleportDecoder : Decoder(4) {

    override suspend fun decode(packet: Source): Instruction? {
        val x = packet.readShortAddLittle()
        val y = packet.readShortLittleEndian().toInt()
        return null
    }
}
