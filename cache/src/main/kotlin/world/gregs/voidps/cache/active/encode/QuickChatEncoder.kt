package world.gregs.voidps.cache.active.encode

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Indices

class QuickChatEncoder : IndexEncoder(Indices.QUICK_CHAT_MESSAGES) {
    override fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        if (archive == 1) {
            writer.writeShort(file)
            super.encode(writer, index, archive, file, data)
        }
    }

    override fun size(cache: Cache): Int {
        val lastArchive = cache.lastArchiveId(index)
        val lastArchive2 = cache.lastArchiveId(Indices.QUICK_CHAT_MENUS)
        return lastArchive * 256 + cache.lastFileId(index, lastArchive) + (lastArchive2 * 256 + cache.lastFileId(index, lastArchive2))
    }
}