package world.gregs.voidps.cache.type

import world.gregs.config.Config
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.Cache
import java.io.File

/**
 * Relevant [Cache] info for loading data for a given [Type]
 */
abstract class TypeLoader<T : Type>(directory: File?, name: String) {
    val base = directory?.resolve("${name}_base.dat")
    val full = directory?.resolve("${name}.dat")
    abstract val index: Int
    open val maxString = 100

    /**
     * @return The number of [Type]s in the [Cache]
     */
    open fun size(cache: Cache) = cache.lastArchiveId(index) * 256 + (cache.fileCount(index, cache.lastArchiveId(index)))

    /**
     * Creates an array of a specified size and populates it with [block].
     */
    abstract fun create(size: Int, block: (Int) -> T? = { null }): Array<T?>

    /**
     * Creates a decoder for the specified [size].
     */
    abstract fun decoder(size: Int): TypeDecoder<T>

    /**
     * Load data from [cache].
     */
    abstract fun data(cache: Cache, index: Int): ByteArray?

    /**
     * Loads data from [cache] and configs [paths] into a type array.
     * [directory] optionally caches into [base] & [full] for faster future loads.
     * @param configInvalidated invalidates [full] cache when any config files were changed
     * @param cacheInvalidated invalidates [base] cache when any config files were changed
     */
    fun load(cache: Cache, paths: List<String>, configInvalidated: Boolean = false, cacheInvalidated: Boolean = false): Array<T?> {
        // No caching
        if (base == null || full == null) {
            return loadFull(cache, paths)
        }
        // Fresh start
        if (!base.exists() || cacheInvalidated) {
            return loadFull(cache, paths)
        }
        // Config missing
        if (!full.exists()) {
            return reloadConfig(paths)
        }
        // Config files were updated
        if (configInvalidated) {
            return reloadConfig(paths)
        }
        // Load fast
        val decoder = loadDirect(full)
        return create(decoder.size) { decoder.create(it) }
    }

    /**
     * Loads data from [cache] and applies config data from [paths].
     * Stores [base] and [full] files for faster loading next time.
     */
    internal fun loadFull(cache: Cache, paths: List<String>): Array<T?> {
        val size = size(cache)
        val decoder = decoder(size)
        val reader = ArrayReader()
        for (i in 0 until size) {
            val data = data(cache, i) ?: continue
            reader.set(data)
            decoder.readPacked(reader, i)
        }
        save(decoder, base)
        applyConfigs(decoder, paths)
        save(decoder, full)
        return create(size) { decoder.create(it) }
    }

    /**
     * Loads data from [base] and applies newer config data from [paths] onto of it.
     * Stores [full] files for fast loading next time.
     */
    internal fun reloadConfig(paths: List<String>): Array<T?> {
        val decoder = loadDirect(base!!)
        applyConfigs(decoder, paths)
        save(decoder, full)
        return create(decoder.size) { decoder.create(it) }
    }

    /**
     * Loads config data from [paths] and applies it to [decoder].
     */
    fun applyConfigs(decoder: TypeDecoder<T>, paths: List<String>) {
        val temp = decoder(1)
        for (path in paths) {
            Config.fileReader(path, maxString) {
                while (nextSection()) {
                    temp.clear()
                    temp.readConfig(this, 0)
                    val id = temp.id.get(0)
                    decoder.override(temp, 0, id)
                }
            }
        }
    }

    /**
     * Writes [decoder] data to [file] in fast and flat binary format.
     */
    internal fun save(decoder: TypeDecoder<T>, file: File?) {
        if (file == null) {
            return
        }
        val size = decoder.directSize()
        val writer = ArrayWriter(size + 4 + 10)
        writer.writeInt(decoder.size)
        decoder.writeDirect(writer)
        file.writeBytes(writer.toArray())
    }

    /**
     * Loads binary format data from [reader].
     */
    fun loadDirect(file: File): TypeDecoder<T> {
        val reader = ArrayReader(file.readBytes())
        val size = reader.readInt()
        val decoder = decoder(size)
        decoder.readDirect(reader)
        return decoder
    }
}