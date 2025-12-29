package world.gregs.voidps.buffer.read

internal class BufferReaderTest : ReaderTest() {
    override fun packet(vararg bytes: Int) {
        buffer = BufferReader(bytes.map { it.toByte() }.toByteArray())
    }

}