package world.gregs.voidps.cache.encode

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Indices

class ClientScriptEncoder : IndexEncoder(Indices.CLIENT_SCRIPTS) {
    override fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        if (archive == 1142) { // style script
            writer.writeShort(archive)
            writer.writeInt(data.size)
            super.encode(writer, index, archive, file, data)
        }
    }
}