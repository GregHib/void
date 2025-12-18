package world.gregs.voidps.cache.type

import world.gregs.config.Config
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import java.io.File

class TypeLoader {
    fun <T: Type> load(config: LoaderConfig<T>, directory: File, cache: Cache, lastCacheChange: Long): Array<T?> {
        val base = directory.resolve("${config.name}_base.dat")
        val combined = directory.resolve("${config.name}.dat")
        // Fresh start
        if (!base.exists()) {
            return loadRaw(config, base, combined, cache)
        }
        // Cache was updated
        val cacheLastUpdated = BufferReader(base.inputStream().readNBytes(8)).readLong()
        if (lastCacheChange > cacheLastUpdated) {
            return loadRaw(config, base, combined, cache)
        }
        // Config missing
        if (!combined.exists()) {
            val reader = BufferReader(base.readBytes())
            reader.skip(8)
            return reloadBinary(config, reader, combined)
        }
        val reader = BufferReader(combined.readBytes())
        val lastUpdated = reader.readLong()
        // Config files were updated
        if (config.lastModified > lastUpdated) {
            val reader = BufferReader(base.readBytes())
            reader.skip(8)
            return reloadBinary(config, reader, combined)
        }
        // Load fast
        return loadBinary(config, reader)
    }

    fun <T: Type> loadRaw(config: LoaderConfig<T>, base: File, combined: File, cache: Cache): Array<T?> {
        val size = config.size(cache)
        val reader = BufferReader()
        val decoder = config.decoder()
        val array: Array<T?> = config.create(size) { id ->
            val data = cache.data(config.index, config.archive(id), config.file(id)) ?: return@create null
            reader.set(data)
            decoder.readBinary(reader)
        }
        save(config, array, base)
        applyConfigs(config, array)
        save(config, array, combined)
        return array
    }

    fun <T: Type> reloadBinary(config: LoaderConfig<T>, reader: BufferReader, combined: File): Array<T?> {
        val array = loadBinary(config, reader)
        applyConfigs(config, array)
        save(config, array, combined)
        return array
    }

    private fun <T: Type> save(config: LoaderConfig<T>, array: Array<T?>, file: File) {
        val writer = BufferWriter(config.bufferSize)
        writer.writeLong(System.currentTimeMillis())
        writer.writeInt(array.size)
        val pointer = writer.position()
        writer.writeInt(0) // Marker
        var count = 0
        val decoder = config.decoder()
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

    private fun <T: Type> applyConfigs(config: LoaderConfig<T>, array: Array<T?>) {
        val decoder = config.decoder()
        val combined = config.decoder()
        val fields = decoder.fieldMap()
        for (path in config.paths) {
            Config.fileReader(path) {
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

    fun <T: Type> loadBinary(config: LoaderConfig<T>, reader: BufferReader): Array<T?> {
        val size = reader.readInt()
        val count = reader.readInt()
        val decoder = config.decoder()
        val array = config.create(size) { null }
        for (i in 0 until count) {
            val type = decoder.readBinary(reader)
            array[type.id] = type
        }
        return array
    }
}