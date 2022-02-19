package world.gregs.voidps.tools.map.xtea

import kotlinx.coroutines.runBlocking
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.XteaLoader
import world.gregs.voidps.engine.map.region.Xteas

object CacheMapDecryption {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val cache = CacheDelegate("./data/cache_decrypted/")
        val xteas = Xteas().apply { XteaLoader().load(this, "./xteas/") }
        var count = 0
        val archives = cache.getArchives(Indices.MAPS).toSet()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val region = Region(regionX, regionY)
                val archive = cache.getArchiveId(Indices.MAPS, "l${regionX}_${regionY}")
                if (!archives.contains(archive)) {
                    continue
                }
                val keys = xteas[region]
                if (keys != null) {
                    val data = cache.getFile(Indices.MAPS, archive, 0, keys)!!
                    cache.write(Indices.MAPS, archive, 0, data)
                    count++
                }
            }
        }
        cache.update()
        println("$count map's decrypted")
    }
}