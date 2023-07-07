package world.gregs.voidps.tools.map

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.engine.map.region.Xteas
import world.gregs.voidps.type.Region
import java.io.File

/**
 * Packs maps that are missing in the target cache when they're found in another cache
 */
object MapPacker {

    @JvmStatic
    fun main(args: Array<String>) {
        val target = CacheDelegate("${System.getProperty("user.home")}/rs634_cache/")
        val xteas = Xteas().load("./data/xteas.dat")
        packMissingMaps(target, xteas, CacheDelegate("${System.getProperty("user.home")}/Downloads/727 cache with most xteas/"), Xteas(), all())
        packMissingMaps(target, xteas, CacheDelegate("${System.getProperty("user.home")}/Downloads/cache-280/"), getKeys(280), all()) // revision 681
        packEaster08Map(target, CacheDelegate("${System.getProperty("user.home")}/Downloads/cache-257/")) // revision 537
    }

    private fun packMissingMaps(target: CacheDelegate, sourceXteas: Xteas, source: CacheDelegate, targetXteas: Xteas, regions: List<Region>) {
        val invalid = mutableSetOf<Region>()
        runBlocking {
            val archives = target.getArchives(Index.MAPS).toSet()
            for (region in regions) {
                val archive = target.getArchiveId(Index.MAPS, "l${region.x}_${region.y}")
                if (!archives.contains(archive)) {
                    continue
                }
                val data = target.getFile(Index.MAPS, archive, 0, sourceXteas[region])
                if (data == null) {
                    val objData = source.getFile(Index.MAPS, "l${region.x}_${region.y}", targetXteas[region])
                    val tileData = source.getFile(Index.MAPS, "m${region.x}_${region.y}")
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
        val objData = source.getFile(Index.MAPS, "l${region.x}_${region.y}", intArrayOf(-929935426, 1005492936, -2143736251, 386758357))
        val tileData = source.getFile(Index.MAPS, "m${region.x}_${region.y}")
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

    private fun getKeys(target: Int): Xteas {
        val file = File("./temp/runescape-${target}.keys")
        val content = if (file.exists()) {
            file.readText()
        } else {
            val text = Jsoup.connect("https://archive.openrs2.org/caches/runescape/${target}/keys.json")
                .ignoreContentType(true)
                .get()
                .body()
                .ownText()
            file.parentFile.mkdirs()
            file.writeText(text)
            text
        }
        return Xteas(Xteas.loadJson(content, value = "key").toMutableMap())
    }

}