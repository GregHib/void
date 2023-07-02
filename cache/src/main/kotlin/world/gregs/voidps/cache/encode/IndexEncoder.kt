package world.gregs.voidps.cache.encode

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import java.io.File

open class IndexEncoder(val index: Int, val config: Int = 0) {
    var crc: Int = -1
    var md5: String = ""
    var outdated: Boolean = true

    open fun size(cache: Cache): Int {
        return cache.lastArchiveId(index) * 256 + (cache.archiveCount(index, cache.lastArchiveId(index)))
    }

    open fun file(directory: File) = directory.resolve("index$index.dat")

    open fun encode(writer: Writer, cache: Cache) {
        writer.writeInt(size(cache))
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