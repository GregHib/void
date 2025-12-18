package world.gregs.voidps.cache.type

import world.gregs.config.Config
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.type.decode.ItemTypeDecoder
import world.gregs.voidps.cache.type.types.ItemType
import java.io.File

abstract class TypeLoader<T: Type>(directory: File, name: String) {
    val base = directory.resolve("${name}_base.dat")
    val combined = directory.resolve("${name}.dat")
    open val bufferSize = 1_000_000
    abstract val index: Int
    open val maxString = 100

    open fun size(cache: Cache) = cache.lastArchiveId(index) * 256 + (cache.fileCount(index, cache.lastArchiveId(index)))

    open fun file(id: Int) = id

    open fun archive(id: Int) = id

    abstract fun create(size: Int, block: (Int) -> T? = { null }): Array<T?>

    abstract fun decoder(): TypeDecoder<T>

    fun load(cache: Cache, paths: List<String>, configInvalidated: Boolean = false, cacheInvalidated: Boolean = false): Array<T?> {
        // Fresh start
        if (!base.exists() || cacheInvalidated) {
            return loadRaw(cache, paths)
        }
        // Config missing
        if (!combined.exists()) {
            return reloadBinary(paths)
        }
        // Config files were updated
        if (configInvalidated) {
            return reloadBinary(paths)
        }
        // Load fast
        val reader = BufferReader(combined.readBytes())
        return loadBinary(reader)
    }

    /**
     * Loads data from [cache] and applies config data from [paths].
     * Stores [base] and [combined] files for faster loading next time.
     */
    fun loadRaw(cache: Cache, paths: List<String>): Array<T?> {
        val size = size(cache)
        val reader = BufferReader()
        val decoder = decoder()
        val array: Array<T?> = create(size) { id ->
            val data = cache.data(index, archive(id), file(id)) ?: return@create null
            reader.set(data)
            decoder.resetFlags()
            decoder.id.value = id
            decoder.loadBinary(reader)
            decoder.create()
        }
        save(array, base)
        applyConfigs(paths, array)
        save(array, combined)
        return array
    }

    /**
     * Loads data from [base] and applies newer config data from [paths] onto of it.
     * Stores [combined] files for fast loading next time.
     */
    fun reloadBinary(paths: List<String>): Array<T?> {
        val reader = BufferReader(base.readBytes())
        val array = loadBinary(reader)
        applyConfigs(paths, array)
        save(array, combined)
        return array
    }

    /**
     * Writes [array] to [file] in binary format.
     */
    private fun save(array: Array<T?>, file: File) {
        val writer = BufferWriter(bufferSize)
        writer.writeInt(array.size)
        val pointer = writer.position()
        writer.writeInt(0) // Marker
        var count = 0
        val decoder = decoder()
        for (type in array) {
            type ?: continue
            decoder.reset()
            decoder.load(type)
            if (decoder.writeBinary(writer)) {
                count++
            }
        }
        val end = writer.position()
        writer.position(pointer)
        writer.writeInt(count)
        writer.position(end)
        file.writeBytes(writer.toArray())
    }

    /**
     * Loads config data from [paths] and applies it onto of [array].
     */
    private fun applyConfigs(paths: List<String>, array: Array<T?>) {
        val decoder = decoder()
        val combined = decoder()
        val fields = decoder.fieldMap()
        for (path in paths) {
            Config.fileReader(path, maxString) {
                while (nextSection()) {
                    decoder.reset()
                    decoder.loadConfig(this, fields)
                    val id = decoder.id.value
                    val current = array[id]
                    if (current == null) {
                        array[id] = decoder.create()
                        continue
                    }
                    combined.reset()
                    combined.load(current)
                    combined.join(decoder)
                    array[id] = combined.create()
                }
            }
        }
    }

    /**
     * Loads binary format data from [reader].
     */
    fun loadBinary(reader: BufferReader): Array<T?> {
        val size = reader.readInt()
        val count = reader.readInt()
        val decoder = decoder()
        val array = create(size) { null }
        for (i in 0 until count) {
            val type = decoder.readBinary(reader)
            array[type.id] = type
        }
        return array
    }
}