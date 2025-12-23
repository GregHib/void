package world.gregs.voidps.engine.data.type

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.type.TypeList
import world.gregs.voidps.cache.type.load.ItemLoader
import world.gregs.voidps.cache.type.types.ItemType
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.tempCache
import world.gregs.voidps.engine.timedLoad
import java.io.File

object ItemTypes : TypeList<ItemType> {
    override var types: Array<ItemType?> = arrayOfNulls(0)
        private set

    override fun empty() = ItemType.EMPTY

    fun load(cache: Cache, files: ConfigFiles, extension: String = Settings["definitions.items"], temp: File? = tempCache()) = timedLoad("item type") {
        val paths = files.list(extension)
        val loader = ItemLoader(temp)
        types = loader.load(cache, paths, files.extensions.contains(extension), files.cacheUpdate)
        types.size
    }
}