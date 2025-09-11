package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import kotlinx.io.Sink
import java.nio.ByteBuffer

class ByteArrayChannel : ByteWriteChannel {
    private val builder = BytePacketBuilder()

    override val closedCause: Throwable?
        get() = null

    @InternalAPI
    override val writeBuffer: Sink = Buffer()

    override suspend fun flush() {
    }

    override suspend fun flushAndClose() {
        TODO("Not yet implemented")
    }

    override fun cancel(cause: Throwable?) {
        TODO("Not yet implemented")
    }

    override val isClosedForWrite: Boolean
        get() = false

    fun toByteArray(): ByteArray = builder.build().readBytes()

}
