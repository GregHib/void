package world.gregs.voidps.cache.encode

import world.gregs.voidps.buffer.write.Writer

class ClientScriptEncoder : IndexEncoder() {
    override fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        if (archive == 1142) { // style script
            super.encode(writer, index, archive, file, data)
        }
    }
}