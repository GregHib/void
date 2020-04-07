package org.redrune.cache.config

import org.redrune.cache.Definition
import org.redrune.cache.DefinitionDecoder
import org.redrune.cache.Indices

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 07, 2020
 */
abstract class ConfigDecoder<T : Definition>(internal val archive: Int) : DefinitionDecoder<T>(Indices.CONFIGS) {
    override val size: Int
        get() = cache.lastFileId(Indices.CONFIGS, archive)

    override fun getArchive(id: Int) = archive
}