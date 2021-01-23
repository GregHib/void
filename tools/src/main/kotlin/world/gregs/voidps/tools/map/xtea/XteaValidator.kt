package world.gregs.voidps.tools.map.xtea

import kotlinx.coroutines.runBlocking
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.XteaLoader
import world.gregs.voidps.cache.Indices

object XteaValidator {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val cache = CacheDelegate("${System.getProperty("user.home")}\\Downloads\\rs634_cache\\", "1", "1")
        val xteas = XteaLoader().run("./xteas/")//"${System.getProperty("user.home")}\\Downloads\\rs634_cache\\634\\")

        val archives = cache.getArchives(Indices.MAPS).toSet()
        var total = 0
        var valid = 0
        val invalid = mutableSetOf<Region>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                val region = Region(regionX, regionY)
                val archive = cache.getArchiveId(Indices.MAPS, "l${regionX}_${regionY}")
                if (!archives.contains(archive)) {
                    continue
                }
                total++
                val data = cache.getFile(Indices.MAPS, archive, 0, xteas[region])
                if (data == null) {
                    if(xteas.containsKey(region.id)) {
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