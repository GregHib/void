package world.gregs.voidps.cache.type.load

import world.gregs.config.Config
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.type.decode.ItemTypeDecoder
import world.gregs.voidps.cache.type.list.ItemTypeList
import world.gregs.voidps.cache.type.types.ItemType
import java.io.File

class ItemTypeLoader(
    val cache: Cache,
    val decoder: ItemTypeDecoder,
) {

    init {
        decoder.check()
    }

    val index = Index.ITEMS

    fun size(cache: Cache): Int = cache.lastArchiveId(index) * 256 + (cache.fileCount(index, cache.lastArchiveId(index)))

    fun file(id: Int) = id and 0xff

    fun archive(id: Int) = id ushr 8

    val MAX_SIZE = 2_000_000

    fun loadRaw(paths: List<String>, base: File, config: File): ItemTypeList {
        val size = size(cache)
        val configs = loadConfigs(paths)
        val baseWriter = BufferWriter(MAX_SIZE)
        baseWriter.writeLong(System.currentTimeMillis())
        val configWriter = BufferWriter(MAX_SIZE)
        configWriter.writeLong(System.currentTimeMillis())
        val reader = BufferReader()
        val array: Array<ItemType?> = Array(size) { id ->
            val data = cache.data(index, archive(id), file(id)) ?: return@Array null
            reader.set(data)
            decoder.resetFlags()
            decoder.loadBinary(reader)
            decoder.writeBinary(baseWriter)
            val override = configs[id]
            if (override != null) {
                decoder.join(override)
            }
            decoder.writeBinary(configWriter)// TODO could just save relevant fields rather than all - if do then need to track when the list of relevant fields changes too
            decoder.create()
        }
        base.writeBytes(baseWriter.toArray())
        config.writeBytes(configWriter.toArray())
        return ItemTypeList(array)
    }

    private fun loadConfigs(paths: List<String>): Map<Int, ItemTypeDecoder> {
        val fields = decoder.fieldMap()
        val configs = mutableMapOf<Int, ItemTypeDecoder>()
        for (path in paths) {
            Config.fileReader(path) {
                while (nextSection()) {
                    val decoder = ItemTypeDecoder()
                    decoder.loadConfig(this, fields)
                    configs[decoder.id.value] = decoder
                }
            }
        }
        return configs
    }

    fun reloadBinary(paths: List<String>, reader: BufferReader, config: File): ItemTypeList {
        val configs = loadConfigs(paths)
        val size = reader.readInt()
        val count = reader.readInt()
        val array = arrayOfNulls<ItemType?>(size)
        val writer = BufferWriter(MAX_SIZE)
        writer.writeLong(System.currentTimeMillis())
        for (i in 0 until count) {
            decoder.resetFlags()
            decoder.loadBinary(reader)
            val id = decoder.id.value
            val override = configs[id]
            if (override != null) {
                decoder.join(override)
            }
            decoder.writeBinary(writer)
            array[id] = decoder.create()
        }
        config.writeBytes(writer.toArray())
        return ItemTypeList(array)
    }

    fun loadBinary(reader: BufferReader): ItemTypeList {
        val size = reader.readInt()
        val count = reader.readInt()
        val array = arrayOfNulls<ItemType?>(size)
        for (i in 0 until count) {
            val type = decoder.readBinary(reader)
            array[type.id] = type
        }
        return ItemTypeList(array)
    }

    fun load(paths: List<String>): ItemTypeList {
        val name = "items"
        val base = File("${name}_base.dat")
        val config = File("${name}.dat")
        // Fresh start
        if (!base.exists()) {
            return loadRaw(paths, base, config)
        }
        val lastCacheChange: Long = File("./data/cache/").lastModified()
        // Cache was updated
        val baseReader = BufferReader(base.readBytes()) // TODO can replace with base.inputStream().readNBytes(8)
        val cacheLastUpdated = baseReader.readLong()
        if (lastCacheChange > cacheLastUpdated) {
            return loadRaw(paths, base, config)
        }
        // Config missing
        if (!config.exists()) {
            return reloadBinary(paths, baseReader, config)
        }
        val lastConfigChange: Long = listOf(File("./data/achievement/"), File("./data/activity/")).maxOf { it.lastModified() } // TODO
        val reader = BufferReader(config.readBytes())
        val lastUpdated = reader.readLong()
        // Config files were updated
        if (lastConfigChange > lastUpdated) {
            return reloadBinary(paths, baseReader, config)
        }
        // Load fast
        return loadBinary(reader)
    }
}