package world.gregs.voidps.cache.encode

import world.gregs.voidps.buffer.write.Writer

class ShiftEncoder(private val shift: Int) : IndexEncoder() {
    override fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        writer.writeInt(file or (archive shl shift))
        super.encode(writer, index, archive, file, data)
    }
}