package world.gregs.voidps.cache.encode

import world.gregs.voidps.buffer.write.Writer

class InterfaceEncoder : IndexEncoder() {
    override fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        writer.writeInt(archive or (file shl 16))
        super.encode(writer, index, archive, file, data)
    }
}