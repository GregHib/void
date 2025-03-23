package world.gregs.voidps.tools.definition.item

import org.apache.commons.io.IOUtils
import org.sweble.wikitext.parser.nodes.WtInternalLink
import org.sweble.wikitext.parser.nodes.WtListItem
import org.sweble.wikitext.parser.nodes.WtPageName
import org.sweble.wikitext.parser.nodes.WtText
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.pipe.extra.ItemDefaults
import world.gregs.voidps.tools.definition.item.pipe.extra.ItemEquipmentInfo
import world.gregs.voidps.tools.definition.item.pipe.extra.ItemManualChanges
import world.gregs.voidps.tools.definition.item.pipe.extra.ItemNoted
import world.gregs.voidps.tools.definition.item.pipe.extra.wiki.*
import world.gregs.voidps.tools.definition.item.pipe.page.LivePageCollector
import world.gregs.voidps.tools.definition.item.pipe.page.OfflinePageCollector
import world.gregs.voidps.tools.definition.item.pipe.page.PageCollector
import world.gregs.voidps.tools.definition.item.pipe.page.UniqueIdentifiers
import world.gregs.voidps.tools.wiki.model.Wiki
import world.gregs.voidps.tools.wiki.model.WikiPage
import world.gregs.voidps.tools.wiki.scrape.RunescapeWiki.export
import world.gregs.yaml.Yaml
import world.gregs.yaml.write.YamlWriterConfiguration
import java.io.File
import java.time.LocalDate
import java.time.Month
import java.util.concurrent.TimeUnit

typealias Extras = Pair<PageCollector, MutableMap<String, Any>>

/**
 * Creates item definition extra values
 * 1. Collects item pages from live rs3 wiki
 * 2. Identifies which item id's correspond with which rs2 and rs3 wikipedia pages
 * 3. Extracts useful information from wikipedia pages into extras: Map<String, Any>
 * 4. Extracts equipment types and slots from a 700+ cache
 * 5. Creates a unique string identifier based on wikipedia page title and item definition name
 * 6. Saves to yaml.
 */
object ItemDefinitionPipeline {

    private val redirectRegex = "#(?:REDIRECT|redirect) ?\\[\\[(.*)]]".toRegex()
    private const val DEBUG_ID = -1

    @JvmStatic
    fun main(args: Array<String>) {

        val rs2Wiki = Wiki.load("${System.getProperty("user.home")}\\Downloads\\runescape_pages_full\\runescapewiki-latest-pages-articles-2011-01-31.xml")
        val cache718 = CacheDelegate("${System.getProperty("user.home")}\\Downloads\\rs718_cache\\")
        val revisionDate = LocalDate.of(2011, Month.JANUARY, 31)// 634

        Settings.load()
        val start = System.currentTimeMillis()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val decoder = ItemDecoder().load(cache)

        val pages = getPages(decoder, rs2Wiki)
        val output = buildItemExtras(revisionDate, decoder, cache718, rs2Wiki, pages)
        val map = convertToYaml(output)
        val yaml = Yaml()
        val config = YamlWriterConfiguration(forceQuoteStrings = true)
        val file = File("items.toml")
        yaml.save(file.path, map, config)
        val contents = "# Don't edit; apply changes to the ItemDefinitionPipeline tool's ItemManualChanges class instead.\n${file.readText()}"
        file.writeText(contents)
        println("${output.size} item definitions written to ${file.path} in ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start)}s")
    }

    private fun buildItemExtras(
        revisionDate: LocalDate,
        decoder: Array<ItemDefinition>,
        cache718: CacheDelegate,
        rs2Wiki: Wiki,
        pages: MutableMap<Int, PageCollector>
    ): MutableMap<Int, Extras> {
        val output = mutableMapOf<Int, Extras>()
        val pipeline = Pipeline<Extras>().apply {
            add(InfoBoxItem(revisionDate))
            add(InfoBoxPet())
            add(InfoBoxConstruction())
            add(InfoBoxMonster())
            add(InfoBoxNPC())
            add(ItemEquipmentInfo(decoder, cache718))
            add(ItemBonuses())
            add(ItemExchangePrices(rs2Wiki))
            add(ItemExchangeLimits())
            add(ItemNoted(decoder))
        }
        for (id in decoder.indices) {
            if (DEBUG_ID >= 0 && id != DEBUG_ID) {
                continue
            }
            val def = decoder.getOrNull(id) ?: continue
            val page = pages[def.id]
            if (page != null) {
                val result = pipeline.modify(page to mutableMapOf())
                val uid = result.first.uid
                if (uid.isNotEmpty() && !uid.startsWith("null", true)) {
                    output[id] = result
                }
            }
        }
        val postProcess = Pipeline<MutableMap<Int, Extras>>().apply {
            add(UniqueIdentifiers())
            add(ItemManualChanges())
            add(ItemDefaults())
        }
        return postProcess.modify(output)
    }

    /**
     * Collects a rs2 and a rs3 page for each [decoder] item id.
     */
    private fun getPages(decoder: Array<ItemDefinition>, rs2Wiki: Wiki): MutableMap<Int, PageCollector> {
        val infoboxes = listOf("infobox item" to "id", "infobox pet" to "itemid")
        val pipeline = Pipeline<PageCollector>().apply {
            add(LivePageCollector("rs3-item", listOf("Items", "Pets"), infoboxes, "runescape.wiki", true) { content, page, idd ->
                content.rs3 = page
                content.rs3Idd = idd
            })
            add(OfflinePageCollector(rs2Wiki, listOf("infobox item", "infobox construction")) { content, page ->
                content.rs2 = page
            })
        }

        val pages = mutableMapOf<Int, PageCollector>()
        val incomplete = mutableListOf<PageCollector>()

        for (id in decoder.indices) {
            if (DEBUG_ID >= 0 && id != DEBUG_ID) {
                continue
            }
            val def = decoder.getOrNull(id) ?: continue
            val processed = pipeline.modify(PageCollector(id, def.name))
            val (_, name, rs2, _, rs3, _) = processed
            if (rs2 == null && rs3 == null && name != "null") {
                incomplete.add(processed)
            } else if (rs2 != null || rs3 != null) {
                pages[id] = processed
            }
        }

        collectUnknownPages("rs2-item", incomplete, rs2Wiki, pages, infoboxes.map { it.first }) { id, page ->
            (pages[id] ?: PageCollector(id, decoder[id].name)).apply {
                rs2 = page
            }
        }
        return pages
    }

    /**
     * Any [incomplete] item ids that the pipeline couldn't find pages for, search the wiki for the exact item def name or potential redirects
     */
    fun collectUnknownPages(
        type: String,
        incomplete: MutableList<PageCollector>,
        wiki: Wiki?,
        pages: MutableMap<Int, PageCollector>,
        infoboxes: List<String>,
        function: (Int, WikiPage) -> PageCollector
    ) {
        val redirects = getRedirects(incomplete.joinToString(separator = "\n") { it.name }, type)
        // Collect those new target pages
        val redirWiki = exportCachedWiki(redirects.values.joinToString(separator = "\n"), "${type}-redirected.xml")
        val redirectPages = mutableMapOf<String, WikiPage>()
        redirWiki.pages.forEach { page ->
            val text = page.revision.text
            infoboxes.forEach {
                if (text.contains(it, true)) {
                    redirectPages[page.title.lowercase()] = page
                }
            }
        }

        // Check unknown items for redirects
        incomplete.forEach {
            val (id, name, _) = it
            val redirect = redirects[name.lowercase()] ?: return@forEach
            val page = wiki?.getExactPageOrNull(redirect) ?: redirectPages[redirect.lowercase()] ?: return@forEach
            pages[id] = function.invoke(id, page)
        }
    }

    /**
     * Returns a map of live page names and their redirect names
     */
    private fun getRedirects(pages: String, type: String): MutableMap<String, String> {
        val wiki = exportCachedWiki(pages, "${type}-redirects.xml")
        val output = mutableMapOf<String, String>()
        wiki.pages.forEach { page ->
            val text = page.revision.text
            if (text.contains(redirectRegex)) {
                val results = redirectRegex.find(text)!!.groupValues[1]
                output[page.title.lowercase()] = results
            } else if (text.contains("otheruses", true)) {
                val template = page.templates.first { it.first.equals("otheruses", true) }
                if (template.second is Map<*, *>) {
                    val map = template.second as Map<*, *>
                    output[page.title.lowercase()] = map[""] as? String ?: return@forEach
                } else {
                    val list = template.second as List<*>
                    if (list.last() is String) {
                        output[page.title.lowercase()] = list.last() as String
                    } else {
                        println("Unknown ${list.last()}")
                    }
                }
            } else if (text.contains("{{disambig}}", true)) {
                val item = page.content.filterIsInstance<WtListItem>().first()
                val result = ((((item.firstOrNull { it is WtInternalLink } ?: return@forEach) as WtInternalLink)[0] as WtPageName)[0] as WtText).content
                output[page.title.lowercase()] = result
            }
        }
        return output
    }

    private fun exportCachedWiki(pages: String, cachePath: String): Wiki {
        val temp = File(cachePath)
        if (!temp.exists()) {
            val export = export(pages)
            IOUtils.copy(export, temp.outputStream())
        }
        return Wiki.loadText(cachePath)
    }

    /**
     * Converts to unique yaml map
     */
    fun convertToYaml(output: MutableMap<Int, Extras>): Map<String, Map<String, Any>> {
        val map = linkedMapOf<String, Map<String, Any>>()
        output
            .mapNotNull { (id, pair) ->
                val (builder, extras) = pair
                extras["id"] = id
                builder.uid to beautify(extras)
            }
            .sortedBy { it.second["id"] as Int }
            .forEach {
                if (map.containsKey(it.first)) {
                    println("Accidental override '${it.first}': ${it.second} - ${map[it.first]}")
                }
                map[it.first] = it.second
            }
        return map

    }

    private fun beautify(extras: MutableMap<String, Any>) = extras
        .toList()
        .groupBy { it.second::class }
        .toList()
        .sortedBy {
            when (it.first) {
                Int::class -> -4
                Boolean::class -> -3
                Double::class -> -2
                String::class -> -1
                else -> 0
            }
        }
        .toMap()
        .map { group -> group.value.sortedBy { it.first.length } }
        .flatten()
        .toMap()
}