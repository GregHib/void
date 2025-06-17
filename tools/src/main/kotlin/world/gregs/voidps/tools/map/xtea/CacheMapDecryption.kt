package world.gregs.voidps.tools.map.xtea

import kotlinx.coroutines.runBlocking
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.tools.cache.Xteas
import world.gregs.voidps.type.Region

object CacheMapDecryption {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val cache = CacheDelegate("./data/cache_decrypted/")
        val xteas = Xteas().load("./xteas/")
        var count = 0
        val archives = cache.archives(Index.MAPS).toSet()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val region = Region(regionX, regionY)
                val archive = cache.archiveId(Index.MAPS, "l${regionX}_$regionY")
                if (!archives.contains(archive)) {
                    continue
                }
                val keys = xteas[region]
                if (keys != null) {
                    val data = cache.data(Index.MAPS, archive, 0, keys)!!
                    cache.write(Index.MAPS, archive, 0, data)
                    count++
                }
            }
        }
        cache.update()
        println("$count map's decrypted")
    }
}
