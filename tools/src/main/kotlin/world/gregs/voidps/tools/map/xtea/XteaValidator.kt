package world.gregs.voidps.tools.map.xtea

import kotlinx.coroutines.runBlocking
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.tools.cache.Xteas
import world.gregs.voidps.type.Region

object XteaValidator {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val cache = CacheDelegate("./data/cache/new/")
        val xteas = Xteas().load("./tools/src/main/resources/xteas.dat", Xteas.DEFAULT_KEY, Xteas.DEFAULT_VALUE)

        val archives = cache.archives(Index.MAPS).toSet()
        var total = 0
        var valid = 0
        val invalid = mutableSetOf<Region>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val region = Region(regionX, regionY)
                val archive = cache.archiveId(Index.MAPS, "l${regionX}_$regionY")
                if (!archives.contains(archive)) {
                    continue
                }
                total++
                val data = cache.data(Index.MAPS, archive, 0, xteas[region])
                if (data == null) {
                    if (xteas.containsKey(region.id)) {
                        println("Failed key ${region.id} $archive ${xteas[region]?.toList()}")
                    }
                    invalid.add(region)
                } else {
                    valid++
                }
                kotlinx.coroutines.delay(10L)
            }
        }
        println("$total map files found.")
        println("${xteas.size} xtea key sets found.")
        println("$valid map files decrypted ${(valid / total.toDouble()) * 100}%")
        println("${invalid.size} missing xtea keys.")
        println(invalid.map { it.id })
    }
}
