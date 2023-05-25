package world.gregs.voidps.tools.map

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Indices
import world.gregs.voidps.engine.map.region.Region
import world.gregs.voidps.engine.map.region.XteaLoader
import world.gregs.voidps.engine.map.region.Xteas
import java.io.File

/**
 * Packs maps that are missing in the target cache when they're found in another cache
 */
object MapPacker {

    @JvmStatic
    fun main(args: Array<String>) {
        val target = CacheDelegate("./data/cache/")
        val xteas = Xteas().apply { XteaLoader().load(this, "./data/xteas.dat") }
        packMissingMaps(target, xteas, CacheDelegate("./727 cache with most xteas/"), Xteas(), all())
        packMissingMaps(target, xteas, CacheDelegate("./cache-280/"), getKeys(280), all()) // 681
    }

    private fun packMissingMaps(target: CacheDelegate, sourceXteas: Xteas, source: CacheDelegate, targetXteas: Xteas, regions: List<Region>) {
        val invalid = mutableSetOf<Region>()
        runBlocking {
            val archives = target.getArchives(Indices.MAPS).toSet()
            for (region in regions) {
                val archive = target.getArchiveId(Indices.MAPS, "l${region.x}_${region.y}")
                if (!archives.contains(archive)) {
                    continue
                }
                val data = target.getFile(Indices.MAPS, archive, 0, sourceXteas[region])
                if (data == null) {
                    val objData = source.getFile(Indices.MAPS, "l${region.x}_${region.y}", targetXteas[region])
                    val tileData = source.getFile(Indices.MAPS, "m${region.x}_${region.y}")
                    if (objData == null || tileData == null) {
                        println("Can't find map $region")
                    } else {
                        println("Written missing map $region")
                        target.write(Indices.MAPS, "l${region.x}_${region.y}", objData)
                        target.write(Indices.MAPS, "m${region.x}_${region.y}", tileData)
                    }
                    invalid.add(region)
                }
                delay(10L)
            }
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
            file.writeText(text)
            text
        }
        return Xteas(XteaLoader().loadJson(content, value = "key").toMutableMap())
    }

}