package world.gregs.voidps.network.login.protocol

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow
import world.gregs.voidps.network.client.Instruction

abstract class Decoder(val length: Int) {

    abstract suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket)

    companion object {
        const val BYTE = -1
        const val SHORT = -2
    }
}