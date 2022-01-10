package world.gregs.voidps.tools.cache

import org.koin.core.context.startKoin
import org.koin.fileProperties
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.engine.map.region.xteaModule
import java.io.File

object DumpMap {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule, xteaModule)
        }.koin
        val cache: Cache = koin.get()
        val xteas: Xteas = koin.get()

        val region = Region(12341)
        val tiles = cache.getFile(Indices.MAPS, "m${region.x}_${region.y}")!!
        val objects = cache.getFile(Indices.MAPS, "l${region.x}_${region.y}", xteas[region])!!
        println("${region.x}_${region.y}")
        File("region${region.id}_tiles.dat").writeBytes(tiles)
        File("region${region.id}_objects.dat").writeBytes(objects)
    }
}