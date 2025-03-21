package world.gregs.voidps.tools.cache

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.type.Region
import java.io.File

object DumpMap {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val xteas = Xteas()//.load("./xteas.json")
        val region = Region(12341)
        val tiles = cache.data(Index.MAPS, "m${region.x}_${region.y}")!!
        val objects = cache.data(Index.MAPS, "l${region.x}_${region.y}", xteas[region])!!
        println("${region.x}_${region.y}")
        File("region${region.id}_tiles.dat").writeBytes(tiles)
        File("region${region.id}_objects.dat").writeBytes(objects)
    }
}