package world.gregs.voidps.network.login.protocol.encode

import io.ktor.utils.io.*
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.*
import java.nio.ByteBuffer

class ByteArrayChannel : ByteWriteChannel {
    private val builder = BytePacketBuilder()

    override val autoFlush: Boolean
        get() = true
    override val availableForWrite: Int
        get() = Int.MAX_VALUE
    override val closedCause: Throwable?
        get() = null
    override val isClosedForWrite: Boolean
        get() = false
    override val totalBytesWritten: Long
        get() = builder.size.toLong()

    fun toByteArray(): ByteArray = builder.build().readBytes()

    override suspend fun writeAvailable(src: ChunkBuffer): Int {
        builder.writeFully(src)
        return 0
    }

    override suspend fun writeAvailable(src: ByteBuffer): Int {
        builder.writeFully(src)
        return src.position()
    }

    override suspend fun writeAvailable(src: ByteArray, offset: Int, length: Int): Int {
        builder.writeFully(src, offset, length)
        return length
    }

    override suspend fun writeByte(b: Byte) {
        builder.writeByte(b)
    }

    override suspend fun writeDouble(d: Double) {
        builder.writeDouble(d)
    }

    override suspend fun writeFloat(f: Float) {
        builder.writeFloat(f)
    }

    override suspend fun writeFully(memory: Memory, startIndex: Int, endIndex: Int) {
        builder.writeFully(memory, startIndex, endIndex)
    }

    override suspend fun writeFully(src: Buffer) {
        builder.writeFully(src)
    }

    override suspend fun writeFully(src: ByteBuffer) {
        builder.writeFully(src)
    }

    override suspend fun writeFully(src: ByteArray, offset: Int, length: Int) {
        builder.writeFully(src, offset, length)
    }

    override suspend fun writeInt(i: Int) {
        builder.writeInt(i)
    }

    override suspend fun writeLong(l: Long) {
        builder.writeLong(l)
    }

    override suspend fun writePacket(packet: ByteReadPacket) {
        builder.writePacket(packet)
    }

    override suspend fun writeShort(s: Short) {
        builder.writeShort(s)
    }

    override fun close(cause: Throwable?): Boolean {
        builder.release()
        return false
    }

    override suspend fun awaitFreeSpace() {
    }

    override fun flush() {
    }

    override suspend fun write(min: Int, block: (ByteBuffer) -> Unit) {
        // Not supported
    }

    override fun writeAvailable(min: Int, block: (ByteBuffer) -> Unit): Int {
        // Not supported
        return 0
    }

    @Deprecated("Use write { } instead.")
    override suspend fun writeSuspendSession(visitor: suspend WriterSuspendSession.() -> Unit) {
        // not supported
    }

    override suspend fun writeWhile(block: (ByteBuffer) -> Boolean) {
        // not supported
    }
}
