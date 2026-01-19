package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Definition

interface DefinitionCodec<T: Definition> {
    val index: Int
    fun create(size: Int, block: (Int) -> T): Array<T>
    fun create(index: Int): T
    fun size(cache: Cache): Int {
        return cache.lastArchiveId(index) * 256 + (cache.fileCount(index, cache.lastArchiveId(index)))
    }
    fun data(cache: Cache, index: Int): ByteArray?
    fun encode(writer: Writer, definition: T)
    fun decode(reader: Reader, definition: T)

    fun load(cache: Cache) = create(size(cache)) {
        val def = create(it)
        val data = data(cache, it)
        if (data != null) {
            decode(ArrayReader(data), def)
        }
        def
    }
}