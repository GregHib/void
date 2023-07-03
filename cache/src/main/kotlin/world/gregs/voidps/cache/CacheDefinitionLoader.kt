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
        decoder.last = size - 1
        val array = decoder.create(size)
        for (id in decoder.indices) {
            val archive = decoder.getArchive(id)
            val file = decoder.getFile(id)
            val data = decoder.getData(cache, archive, file) ?: continue
            array[id].id = id
            decoder.load(cache, archive, file, array, BufferReader(data))
        }
        logger.info { "$size ${decoder::class.simpleName} definitions loaded in ${System.currentTimeMillis() - start}ms" }
        return array
    }
}