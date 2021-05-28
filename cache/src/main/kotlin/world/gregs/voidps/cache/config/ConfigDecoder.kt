package world.gregs.voidps.cache.config

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices

abstract class ConfigDecoder<T : Definition>(cache: Cache, internal val archive: Int) : DefinitionDecoder<T>(cache, Indices.CONFIGS) {
    override val last: Int
        get() = cache.lastFileId(Indices.CONFIGS, archive)

    override fun getArchive(id: Int) = archive
}