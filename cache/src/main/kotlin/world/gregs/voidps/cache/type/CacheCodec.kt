package world.gregs.voidps.cache.type

import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import java.io.File

abstract class CacheCodec<T : Type> {
    abstract val index: Int

    abstract fun create(size: Int, block: (Int) -> T): Array<T>

    abstract fun create(id: Int): T

    open fun group(index: Int): Int = index

    open fun file(index: Int): Int = 0

    fun data(cache: Cache, index: Int) = cache.data(index, group(index), file(index))

    fun size(cache: Cache): Int {
        return cache.lastArchiveId(index) * 256 + (cache.fileCount(index, cache.lastArchiveId(index)))
    }

    fun save(cache: Cache, file: File, maxSize: Int = 1_000_000) {
        val writer = ArrayWriter(maxSize)
        val size = size(cache)
        writer.writeInt(size)
        for (i in 0 until size) {
            val data = data(cache, i)
            if (data == null) {
                writer.writeByte(0)
            } else {
                writer.writeBytes(data)
            }
        }
        file.writeBytes(writer.toArray())
    }

    /**
     * Write multiple definitions to the cache
     */
    fun write(cache: Cache, definitions: Array<T>, maxSize: Int = 1_000) {
        val writer = ArrayWriter(maxSize)
        for (definition in definitions) {
            writer.position(0)
            write(cache, definition, maxSize, writer)
        }
    }

    /**
     * Write a single definition to the cache
     */
    fun write(cache: Cache, definition: T, maxSize: Int = 1_000, writer: Writer = ArrayWriter(maxSize)) {
        definition.encode(writer)
        cache.write(index, group(definition.id), file(definition.id), writer.toArray())
    }

    /**
     * Loads definitions from game [cache]
     * When caching is active definitions are stored in a binary file [tempPath] to be
     * used for faster following loads.
     */
    fun read(
        cache: Cache,
        tempPath: String,
        caching: Boolean = true,
        invalidate: Boolean = false,
        maxDefCacheSize: Int = 1_000_000,
    ): Array<T> {
        if (!caching) {
            return read(cache)
        }
        val file = File(tempPath)
        if (!file.exists() || invalidate) {
            save(cache, file, maxDefCacheSize)
        }
        return read(file)
    }

    private fun read(file: File): Array<T> {
        val reader = ArrayReader(file.readBytes())
        return create(reader.readInt()) {
            val definition = create(it)
            definition.decode(reader)
            definition
        }
    }

    private fun read(cache: Cache): Array<T> {
        val reader = ArrayReader()
        return create(size(cache)) {
            val data = data(cache, it)
            val definition = create(it)
            if (data != null) {
                reader.set(data)
                definition.decode(reader)
            }
            definition
        }
    }

}