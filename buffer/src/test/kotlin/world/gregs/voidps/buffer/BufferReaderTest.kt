package world.gregs.voidps.buffer

import world.gregs.voidps.buffer.read.BufferReader
import java.nio.ByteBuffer

class BufferReaderTest : ReaderTest() {
    override fun packet(vararg bytes: Int) {
        buffer = BufferReader(buffer = ByteBuffer.wrap(bytes.map { it.toByte() }.toByteArray()))
    }
}