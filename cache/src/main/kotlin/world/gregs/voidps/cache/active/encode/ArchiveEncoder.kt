package world.gregs.voidps.cache.active.encode

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.active.ActiveIndexEncoder

class ArchiveEncoder(index: Int) : ActiveIndexEncoder(index) {

    override fun size(cache: Cache): Int {
        return cache.lastArchiveId(index)
    }

    override fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        writer.writeInt(archive)
        super.encode(writer, index, archive, file, data)
    }
}