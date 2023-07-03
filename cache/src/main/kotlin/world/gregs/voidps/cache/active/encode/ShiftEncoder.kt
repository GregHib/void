package world.gregs.voidps.cache.active.encode

import world.gregs.voidps.buffer.write.Writer

class ShiftEncoder(index: Int, private val shift: Int) : IndexEncoder(index) {
    override fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        writer.writeInt(file or (archive shl shift))
        super.encode(writer, index, archive, file, data)
    }
}