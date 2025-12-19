package world.gregs.voidps.buffer

import world.gregs.voidps.buffer.write.ArrayWriter

internal class ArrayWriterTest : WriterTest() {
    override fun writer() = ArrayWriter()
}
