package world.gregs.voidps.cache.config

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index

abstract class ConfigDecoder<T : Definition>(internal val archive: Int) : DefinitionDecoder<T>(Index.CONFIGS) {

    override fun getArchive(id: Int) = archive

    override fun readId(reader: Reader): Int = reader.readShort()

    override fun size(cache: Cache): Int = cache.lastFileId(Index.CONFIGS, archive)
}
