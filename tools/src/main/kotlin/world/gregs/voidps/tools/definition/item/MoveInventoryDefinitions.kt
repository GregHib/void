package world.gregs.voidps.tools.definition.item

import com.displee.cache.CacheLibrary
import world.gregs.voidps.cache.Config
import world.gregs.voidps.cache.Index
import world.gregs.voidps.engine.data.Settings

object MoveInventoryDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache718 = CacheLibrary("${System.getProperty("user.home")}/Downloads/rs718_cache/")
        val cache634 = CacheLibrary(Settings["storage.cache.path"])

        val archive718 = cache718.index(Index.CONFIGS).archive(Config.INVENTORIES)!!
        val archive634 = cache634.index(Index.CONFIGS).archive(Config.INVENTORIES)!!

        val size = archive634.last()?.id ?: -1

        for (i in 0 until size) {
            archive634.add(archive718.file(i) ?: continue)
        }
        cache634.update()
        println("Done")
    }
}