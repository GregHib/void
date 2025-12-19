package world.gregs.voidps.buffer

import world.gregs.voidps.buffer.write.BufferWriter

internal class BufferWriterTest : WriterTest() {
    override fun writer() = BufferWriter()
}
