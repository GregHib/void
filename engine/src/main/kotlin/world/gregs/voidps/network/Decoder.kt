package world.gregs.voidps.network

import io.ktor.utils.io.core.*
import kotlinx.coroutines.flow.MutableSharedFlow

abstract class Decoder(val length: Int) {

    abstract suspend fun decode(instructions: MutableSharedFlow<Instruction>, packet: ByteReadPacket)

    companion object {
        const val BYTE = -1
        const val SHORT = -2
    }
}