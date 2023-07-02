package world.gregs.voidps.cache.config

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Definition
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices

abstract class ConfigDecoder<T : Definition>(internal val archive: Int) : DefinitionDecoder<T>(Indices.CONFIGS) {

    override fun getArchive(id: Int) = archive

    override fun fileName(): String {
        return "config${archive}.dat"
    }

    override fun readId(reader: Reader): Int {
        return reader.readShort()
    }

    override fun id(archive: Int, file: Int): Int {
        return file
    }

    override fun size(cache: Cache): Int {
        return cache.lastFileId(Indices.CONFIGS, archive)
    }
}