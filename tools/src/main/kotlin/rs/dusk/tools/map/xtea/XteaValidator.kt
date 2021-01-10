package rs.dusk.tools.map.xtea

import rs.dusk.cache.CacheDelegate
import rs.dusk.cache.Indices
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.XteaLoader

object XteaValidator {
    @JvmStatic
    fun main(args: Array<String>) {
        val cache = CacheDelegate("./cache/data/cache/", "1", "1")
        val xteas = XteaLoader().run("./cache/data/xteas.dat")

        val archives = cache.getArchives(Indices.MAPS).toSet()
        var total = 0
        var valid = 0
        val invalid = mutableSetOf<Region>()
        val useful = mutableSetOf<Region>()
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
                    invalid.add(region)
                    cache.getFile(Indices.MAPS, "m${regionX}_${regionY}") ?: continue
                    useful.add(region)
                } else {
                    valid++
                }
            }
        }
        println("$total map files found.")
        println("${xteas.size} xtea key sets found.")
        println("$valid map files decrypted ${(valid / total.toDouble()) * 100}%")
        println("${invalid.size} missing xtea keys, ${useful.size} potentially useful ${(useful.size / invalid.size.toDouble()) * 100}%.")
    }
}