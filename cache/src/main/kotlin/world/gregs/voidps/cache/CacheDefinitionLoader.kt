package world.gregs.voidps.cache

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.buffer.read.BufferReader

class CacheDefinitionLoader(
    private val cache: Cache
) : DefinitionLoader {
    private val logger = InlineLogger()

    override fun <T : Definition> load(decoder: DefinitionDecoder<T>): Array<T> {
        val start = System.currentTimeMillis()
        val size = decoder.size(cache) + 1
        val array = decoder.create(size)
        for (id in decoder.indices) {
            val archive = decoder.getArchive(id)
            val file = decoder.getFile(id)
            val data = cache.getFile(decoder.index, archive, file) ?: continue
            array[id].id = id
            decoder.load(archive, file, array, BufferReader(data))
        }
        logger.info { "${this::class.simpleName} loaded $size in ${System.currentTimeMillis() - start}ms" }
        return array
    }
}