package world.gregs.voidps.engine.data.type

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.type.TypeList
import world.gregs.voidps.cache.type.load.ItemLoader
import world.gregs.voidps.cache.type.types.ItemType
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.timedLoad
import java.io.File

object ItemTypes : TypeList<ItemType> {
    override var types: Array<ItemType?> = arrayOfNulls(0)
        private set

    override fun empty() = ItemType.EMPTY

    fun load(cache: Cache, files: ConfigFiles, extension: String = Settings["definitions.items"]) = timedLoad("item type") {
        val paths = files.list(extension)
        val directory = File("./data/cache/temp/")
        directory.mkdirs()
        val loader = ItemLoader(directory)
        types = loader.load(cache, paths, files.extensions.contains(extension), files.cacheUpdate)
//        val writer = BufferWriter(8)
//        writer.writeLong(System.currentTimeMillis())
//        File(Settings["storage.data.modified"]).writeBytes(writer.toArray())
        types.size
    }
}