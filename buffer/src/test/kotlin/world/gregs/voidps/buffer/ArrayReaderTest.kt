package world.gregs.voidps.buffer

import world.gregs.voidps.buffer.read.ArrayReader

class ArrayReaderTest : ReaderTest() {
    override fun packet(vararg bytes: Int) {
        buffer = ArrayReader(bytes.map { it.toByte() }.toByteArray())
    }
}