package world.gregs.voidps.cache.config

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.active.ActiveCache

abstract class ConfigDecoder<T : Definition>(internal val archive: Int) : DefinitionDecoder<T>(Index.CONFIGS) {

    override fun getArchive(id: Int) = archive

    override fun fileName() = ActiveCache.configFile(archive)

    override fun readId(reader: Reader): Int {
        return reader.readShort()
    }

    override fun size(cache: Cache): Int {
        return cache.lastFileId(Index.CONFIGS, archive)
    }
}