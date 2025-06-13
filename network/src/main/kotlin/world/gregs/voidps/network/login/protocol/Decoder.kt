package world.gregs.voidps.network.login.protocol

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction

abstract class Decoder(val length: Int) {

    abstract suspend fun decode(packet: ByteReadPacket): Instruction?

    companion object {
        const val BYTE = -1
        const val SHORT = -2
    }
}
