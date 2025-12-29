package world.gregs.voidps.buffer.read

class ArrayReaderTest : ReaderTest() {
    override fun packet(vararg bytes: Int) {
        buffer = ArrayReader(bytes.map { it.toByte() }.toByteArray())
    }
}