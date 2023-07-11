package world.gregs.voidps.cache.active.encode

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.active.ActiveIndexEncoder
import world.gregs.voidps.cache.definition.data.InterfaceDefinition

class InterfaceEncoder : ActiveIndexEncoder(Index.INTERFACES) {

    override fun encode(writer: Writer, cache: Cache) {
        writer.writeInt(size(cache))
        for (archiveId in cache.getArchives(index).reversed()) {
            val files = cache.getArchiveData(index, archiveId) ?: continue
            for (fileId in files.keys.reversed()) {
                val data = files.getValue(fileId) ?: continue
                encode(writer, index, archiveId, fileId, data)
            }
        }
    }

    override fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        writer.writeInt(InterfaceDefinition.pack(archive, file))
        super.encode(writer, index, archive, file, data)
    }
}