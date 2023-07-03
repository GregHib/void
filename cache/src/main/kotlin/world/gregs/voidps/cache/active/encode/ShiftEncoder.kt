package world.gregs.voidps.cache.active.encode

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.active.ActiveIndexEncoder

class ShiftEncoder(index: Int, private val shift: Int) : ActiveIndexEncoder(index) {
    override fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        writer.writeInt(file or (archive shl shift))
        super.encode(writer, index, archive, file, data)
    }
}