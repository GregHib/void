package world.gregs.voidps.tools.cache

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.tools.property
import world.gregs.voidps.type.Region
import java.io.File

object DumpMap {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("storage.cache.path"))
        val xteas: Xteas = Xteas().load()
        val region = Region(12341)
        val tiles = cache.data(Index.MAPS, "m${region.x}_${region.y}")!!
        val objects = cache.data(Index.MAPS, "l${region.x}_${region.y}", xteas[region])!!
        println("${region.x}_${region.y}")
        File("region${region.id}_tiles.dat").writeBytes(tiles)
        File("region${region.id}_objects.dat").writeBytes(objects)
    }
}