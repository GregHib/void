package world.gregs.void.cache.config

import world.gregs.void.cache.Cache
import world.gregs.void.cache.Definition
import world.gregs.void.cache.DefinitionDecoder
import world.gregs.void.cache.Indices

/**
 * @author GregHib <greg@gregs.world>
 * @since April 07, 2020
 */
abstract class ConfigDecoder<T : Definition>(cache: Cache, internal val archive: Int) : DefinitionDecoder<T>(cache, Indices.CONFIGS) {
    override val size: Int
        get() = cache.lastFileId(Indices.CONFIGS, archive)

    override fun getArchive(id: Int) = archive
}