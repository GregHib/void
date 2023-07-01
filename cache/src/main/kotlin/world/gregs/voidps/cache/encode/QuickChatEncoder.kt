package world.gregs.voidps.cache.encode

import world.gregs.voidps.buffer.write.Writer

class QuickChatEncoder : IndexEncoder() {
    override fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        if (archive == 1) {
            writer.writeShort(file)
            super.encode(writer, index, archive, file, data)
        }
    }
}