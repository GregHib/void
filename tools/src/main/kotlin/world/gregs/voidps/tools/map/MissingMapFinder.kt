package world.gregs.voidps.tools.map

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index
import world.gregs.voidps.tools.cache.Xteas
import world.gregs.voidps.type.Region
import java.io.File

private data class Cache(
    val id: Int,
    val scope: String,
    val game: String,
    val language: String,
    val builds: List<Map<String, Int>>,
)

/**
 * Searches all keys in openrs2.org to find which caches missing maps can be found in for a specified revision
 */
object MissingMapFinder {

    private val temp = File("./temp/xteas/")
    private val json = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    private val results = temp.resolve("results")
    private const val REVISION = 634

    /**
     * Prints out only for caches which exist
     */
    private const val SHOW_EXISTING_ONLY = false

    /**
     * Specify compatible revisions to search within
     */
    private val revisionRange = 500..800

    @JvmStatic
    fun main(args: Array<String>) {
        val cache = CacheDelegate("./temp/cache_packed/")

        val caches = getCacheInfo()
            .filter { c -> c.game == "runescape" && c.language == "en" }
        println("Found ${caches.size} cache revisions.")
        val target = caches
            .first { c -> c.builds.any { build -> build["major"] == REVISION } }
        println("Identified $target for revision $REVISION")
        val xteas = getKeys(target)
        println("Loaded ${xteas.size} keys.")

        val invalids = getInvalidKeys(cache, xteas).toMutableSet()
        println("Identified ${invalids.size} invalid maps in cache.")

        val index = caches.indexOf(target)
        println("Target revision: $REVISION")
        for (i in caches.indices) {
            search(caches, index + i, invalids)
            search(caches, index - i, invalids)
        }

        val directory = temp.resolve("caches/")
        for ((region, list) in found) {
            val sorted = list.sortedByDescending { majorVersion(it.first).compareTo(REVISION) }
            if (SHOW_EXISTING_ONLY) {
                val remaining = sorted.filter { directory.resolve("cache-${it.first.id}/").exists() }
                print(region, remaining)
            } else {
                print(region, sorted)
            }
        }
        println("Remaining regions might still be available in high revision unencrypted caches.")
    }

    private fun print(region: Int, list: List<Pair<Cache, IntArray>>) {
        println(
            "$region - ${list.first().second.contentToString()} https://archive.openrs2.org/caches/runescape/${list.first().first.id} - ${
                list.map {
                    "${it.first.id}, ${
                        it.first.builds.getOrNull(0)
                    }, ${it.second.contentToString()}"
                }
            }",
        )
    }

    private fun majorVersion(cache: Cache) = cache.builds.getOrNull(0)?.get("major") ?: Int.MIN_VALUE

    private val found = mutableMapOf<Int, MutableList<Pair<Cache, IntArray>>>()

    private fun search(caches: List<Cache>, index: Int, invalids: MutableSet<Region>) {
        val cache = caches.getOrNull(index) ?: return
        if (majorVersion(cache) !in revisionRange) {
            return
        }
        val keys = getKeys(cache)
        invalids.forEach { invalid ->
            if (keys.containsKey(invalid.id)) {
                val array = keys[invalid.id]!!
                found.getOrPut(invalid.id) { mutableListOf() }.add(cache to array)
                results.appendText("${invalid.id},${cache.id},${array[0]},${array[1]},${array[2]},${array[3]}\n")
                println("Found invalid keys for region ${invalid.id} $cache ${keys[invalid.id].contentToString()}")
            }
        }
    }

    private fun getCacheInfo(): Array<Cache> {
        val content = Jsoup.connect("https://archive.openrs2.org/caches.json")
            .ignoreContentType(true)
            .get()
            .body()
            .ownText()
        return json.readValue<Array<Cache>>(content)
    }

    private fun getInvalidKeys(cache: CacheDelegate, xteas: Xteas): Set<Region> {
        val file = temp.resolve("invalid.maps")
        if (file.exists()) {
            return file.readLines().map { Region(it.toInt()) }.toSet()
        }
        val invalid = mutableSetOf<Region>()
        runBlocking {
            val archives = cache.archives(Index.MAPS).toSet()
            for (regionX in 0 until 256) {
                for (regionY in 0 until 256) {
                    val region = Region(regionX, regionY)
                    val archive = cache.archiveId(Index.MAPS, "l${regionX}_$regionY")
                    if (!archives.contains(archive)) {
                        continue
                    }
                    val data = cache.data(Index.MAPS, archive, 0, xteas[region])
                    if (data == null) {
                        invalid.add(region)
                    }
                    delay(10L)
                }
            }
        }
        file.writeText(invalid.map { it.id }.joinToString(separator = "\n"))
        return invalid
    }

    private fun getKeys(target: Cache): Xteas {
        val file = temp.resolve("${target.scope}-${target.id}.keys.json")
        val content = if (file.exists()) {
            file.readText()
        } else {
            val text = Jsoup.connect("https://archive.openrs2.org/caches/${target.scope}/${target.id}/keys.json")
                .ignoreContentType(true)
                .get()
                .body()
                .ownText()
            file.writeText(text)
            text
        }
        return Xteas(Xteas.loadJson(content, value = "key").toMutableMap())
    }
}
