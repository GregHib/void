package rs.dusk.cache.config

import rs.dusk.cache.Definition
import rs.dusk.cache.DefinitionDecoder
import rs.dusk.cache.Indices

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
abstract class ConfigDecoder<T : Definition>(internal val archive: Int) : DefinitionDecoder<T>(Indices.CONFIGS) {
    override val size: Int
        get() = cache.lastFileId(Indices.CONFIGS, archive)

    override fun getArchive(id: Int) = archive
}