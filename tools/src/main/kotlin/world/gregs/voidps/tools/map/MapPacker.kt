package world.gregs.voidps.tools.map

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.tools.cache.OpenRS2
import world.gregs.voidps.tools.cache.Xteas
import world.gregs.voidps.type.Region
import java.io.File

/**
 * Packs maps that are missing in the target cache when they're found in another cache
 */
object MapPacker {

    fun pack634(target: File, targetXteas: Xteas, cache727: File, xteas727: Xteas, cache681: File, xteas681: Xteas, cache537: File) {
        val cache = CacheDelegate(target.path)
        packMissingMaps(cache, targetXteas, CacheDelegate(cache727.path), xteas727, all())
        packMissingMaps(cache, targetXteas, CacheDelegate(cache681.path), xteas681, all()) // revision 681
        packEaster08Map(cache, CacheDelegate(cache537.path)) // revision 537
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val target = File("${System.getProperty("user.home")}/Downloads/rs634_cache/")
        val xteas = Xteas().load("./tools/src/main/resources/xteas.dat", Xteas.DEFAULT_KEY, Xteas.DEFAULT_VALUE)
        val cache727 = File("${System.getProperty("user.home")}/Downloads/727 cache with most xteas/")
        val cache681 = File("${System.getProperty("user.home")}/Downloads/cache-280/")
        val xteas681 = OpenRS2.getKeys(280)
        val cache537 = File("${System.getProperty("user.home")}/Downloads/cache-257/")
        pack634(target, xteas, cache727, Xteas(), cache681, xteas681, cache537)
    }

    private fun packMissingMaps(target: CacheDelegate, sourceXteas: Xteas, source: CacheDelegate, targetXteas: Xteas, regions: List<Region>) {
        val invalid = mutableSetOf<Region>()
        runBlocking {
            val archives = target.archives(Index.MAPS).toSet()
            for (region in regions) {
                val archive = target.archiveId(Index.MAPS, "l${region.x}_${region.y}")
                if (!archives.contains(archive)) {
                    continue
                }
                val data = target.data(Index.MAPS, archive, 0, sourceXteas[region])
                if (data == null) {
                    val objData = source.data(Index.MAPS, "l${region.x}_${region.y}", targetXteas[region])
                    val tileData = source.data(Index.MAPS, "m${region.x}_${region.y}")
                    if (objData == null || tileData == null) {
                        println("Can't find map $region")
                    } else {
                        println("Written missing map $region")
                        target.write(Index.MAPS, "m${region.x}_${region.y}", tileData)
                        target.write(Index.MAPS, "l${region.x}_${region.y}", objData)
                    }
                    invalid.add(region)
                }
                delay(10L)
            }
        }
        target.update()
    }

    private fun packEaster08Map(target: CacheDelegate, source: CacheDelegate) {
        val region = Region(9811)
        val objData = source.data(Index.MAPS, "l${region.x}_${region.y}", intArrayOf(-929935426, 1005492936, -2143736251, 386758357))
        val tileData = source.data(Index.MAPS, "m${region.x}_${region.y}")
        if (objData == null || tileData == null) {
            println("Can't find map $region")
        } else {
            val newRegion = Region(9555)
            println("Written missing map $newRegion")
            target.write(Index.MAPS, "m${newRegion.x}_${newRegion.y}", tileData)
            target.write(Index.MAPS, "l${newRegion.x}_${newRegion.y}", objData)
        }
        target.update()
    }

    private fun all(): List<Region> {
        val list = mutableListOf<Region>()
        for (regionX in 0 until 256) {
            for (regionY in 0 until 256) {
                list.add(Region(regionX, regionY))
            }
        }
        return list
    }
}
