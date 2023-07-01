package world.gregs.voidps.cache.encode

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache

open class IndexEncoder {
    var crc: Int = -1
    var md5: String = ""
    var outdated: Boolean = true

    open fun encode(writer: Writer, cache: Cache, index: Int) {
        for (archiveId in cache.getArchives(index)) {
            val files = cache.getArchiveData(index, archiveId) ?: continue
            for ((fileId, data) in files) {
                if (data == null) {
                    continue
                }
                encode(writer, index, archiveId, fileId, data)
            }
        }
    }

    open fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        writer.writeBytes(data)
    }
}