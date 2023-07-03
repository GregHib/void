package world.gregs.voidps.cache.active.encode

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.cache.active.ActiveCache
import world.gregs.voidps.cache.active.ActiveIndexEncoder
import java.io.File

class ConfigEncoder(config: Int) : ActiveIndexEncoder(Indices.CONFIGS, config) {

    override fun size(cache: Cache): Int {
        return cache.lastFileId(Indices.CONFIGS, config)
    }

    override fun file(directory: File): File {
        return directory.resolve(ActiveCache.configFile(config))
    }

    override fun encode(writer: Writer, index: Int, archive: Int, file: Int, data: ByteArray) {
        if (archive == config) {
            writer.writeShort(file)
            super.encode(writer, index, archive, file, data)
        }
    }
}