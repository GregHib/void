package rs.dusk.tools.definition.item

import org.apache.commons.io.IOUtils
import org.koin.core.context.startKoin
import org.sweble.wikitext.parser.nodes.WtInternalLink
import org.sweble.wikitext.parser.nodes.WtListItem
import org.sweble.wikitext.parser.nodes.WtPageName
import org.sweble.wikitext.parser.nodes.WtText
import rs.dusk.cache.CacheDelegate
import rs.dusk.cache.definition.decoder.ItemDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.pipe.extra.ItemDefaults
import rs.dusk.tools.definition.item.pipe.extra.ItemEquipmentInfo
import rs.dusk.tools.definition.item.pipe.extra.ItemManualChanges
import rs.dusk.tools.definition.item.pipe.extra.ItemNoted
import rs.dusk.tools.definition.item.pipe.extra.wiki.*
import rs.dusk.tools.definition.item.pipe.page.ItemPageRS3Wiki
import rs.dusk.tools.definition.item.pipe.page.ItemPageWiki
import rs.dusk.tools.wiki.model.Wiki
import rs.dusk.tools.wiki.model.WikiPage
import rs.dusk.tools.wiki.scrape.RunescapeWiki.export
import java.io.File
import java.time.LocalDate
import java.time.Month
import java.util.concurrent.TimeUnit

typealias ItemExtras = Pair<ItemDefinitionPipeline.PageCollector, MutableMap<String, Any>>

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

    data class PageCollector(val id: Int, val name: String, val page: WikiPage?, val rs3Page: WikiPage?, val idd: Boolean, var uid: String)

    private val redirectRegex = "#(?:REDIRECT|redirect) ?\\[\\[(.*)]]".toRegex()
    private const val debugId = -1

    @JvmStatic
    fun main(args: Array<String>) {

        val rs2Wiki = Wiki.load("${System.getProperty("user.home")}\\Downloads\\runescapewiki-latest-pages-articles-2011-08-14.xml")
        val cache718 = CacheDelegate("${System.getProperty("user.home")}\\Downloads\\rs718_cache\\", "1", "1")
        val revisionDate = LocalDate.of(2011, Month.OCTOBER, 4)// 667

        val start = System.currentTimeMillis()
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = ItemDecoder(koin.get())

        val pages = getPages(decoder, rs2Wiki)
        val output = buildItemExtras(revisionDate, decoder, cache718, rs2Wiki, pages)
        val map = convertToYaml(output)
        val loader = FileLoader(true)
        val file = File("item-definition-extras.yml")
        loader.save(file, map)
        val contents = "# Don't edit; apply changes to the ItemDefinitionPipeline tool's ItemManualChanges class instead.\n${file.readText()}"
        file.writeText(contents)
        println("${output.size} item definitions written to ${file.path} in ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start)}s")
    }

    private fun buildItemExtras(
        revisionDate: LocalDate,
        decoder: ItemDecoder,
        cache718: CacheDelegate,
        rs2Wiki: Wiki,
        pages: MutableMap<Int, PageCollector>
    ): MutableMap<Int, ItemExtras> {
        val output = mutableMapOf<Int, ItemExtras>()
        val pipeline = Pipeline<ItemExtras>().apply {
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
            add(ItemManualChanges())
            add(ItemDefaults())
        }
        repeat(decoder.size) { id ->
            if(debugId > 0 && id != debugId) {
                return@repeat
            }
            val def = decoder.getOrNull(id) ?: return@repeat
            val page = pages[def.id]
            if (page != null) {
                val result = pipeline.modify(page to mutableMapOf())
                val (builder, extras) = result
                if (builder.uid.isNotEmpty() || extras.isNotEmpty()) {
                    output[id] = result
                }
            }
        }
        return output
    }

    /**
     * Collects a rs2 and an rs3 page for each [decoder] item id.
     */
    private fun getPages(decoder: ItemDecoder, rs2Wiki: Wiki): MutableMap<Int, PageCollector> {
        val pipeline = Pipeline<PageCollector>().apply {
            add(ItemPageRS3Wiki())
            add(ItemPageWiki(rs2Wiki))
        }

        val pages = mutableMapOf<Int, PageCollector>()
        val incomplete = mutableListOf<PageCollector>()

        repeat(decoder.size) { id ->
            if(debugId > 0 && id != debugId) {
                return@repeat
            }
            val def = decoder.getOrNull(id) ?: return@repeat
            val processed = pipeline.modify(PageCollector(id, def.name, null, null, false, ""))
            val (_, name, page, rs3, _) = processed
            if (page == null && rs3 == null && name != "null") {
                incomplete.add(processed)
            } else if (page != null || rs3 != null) {
                pages[id] = processed
            }
        }

        collectUnknownPages(incomplete, rs2Wiki, pages, decoder)
        return pages
    }

    /**
     * Any [incomplete] item ids that the pipeline couldn't find pages for, search the wiki for the exact item def name or potential redirects
     */
    private fun collectUnknownPages(
        incomplete: MutableList<PageCollector>,
        rs2Wiki: Wiki,
        pages: MutableMap<Int, PageCollector>,
        decoder: ItemDecoder
    ) {
        val redirects = getRedirects(incomplete.joinToString(separator = "\n") { it.name })
        // Collect those new target pages
        val wiki = exportCachedWiki(redirects.values.joinToString(separator = "\n"), "redirected.xml")
        val redirectPages = mutableMapOf<String, WikiPage>()
        wiki.pages.forEach { page ->
            val text = page.revision.text
            if (text.contains("infobox item", true) || text.contains("infobox pet", true)) {
                redirectPages[page.title.toLowerCase()] = page
            }
        }

        // Check unknown items for redirects
        incomplete.forEach {
            val (id, name, _) = it
            val redirect = redirects[name.toLowerCase()] ?: return@forEach
            val page = rs2Wiki.getExactPageOrNull(redirect) ?: redirectPages[redirect.toLowerCase()] ?: return@forEach
            pages[id] = if (pages.containsKey(id)) {
                pages[id]!!.copy(page = page)
            } else {
                PageCollector(id, decoder.get(id).name, page, null, false, "")
            }
        }
    }

    /**
     * Returns a map of live page names and their redirect names
     */
    private fun getRedirects(pages: String): MutableMap<String, String> {
        val wiki = exportCachedWiki(pages, "redirects.xml")
        val output = mutableMapOf<String, String>()
        wiki.pages.forEach { page ->
            val text = page.revision.text
            if (text.contains(redirectRegex)) {
                val results = redirectRegex.find(text)!!.groupValues[1]
                output[page.title.toLowerCase()] = results
            } else if (text.contains("otheruses", true)) {
                val template = page.templates.first { it.first.equals("otheruses", true) }
                if (template.second is Map<*, *>) {
                    val map = template.second as Map<*, *>
                    output[page.title.toLowerCase()] = map[""] as? String ?: return@forEach
                } else {
                    val list = template.second as List<*>
                    output[page.title.toLowerCase()] = list.last() as String
                }
            } else if (text.contains("{{disambig}}", true)) {
                val item = page.content.filterIsInstance<WtListItem>().first()
                val result = (((item.first { it is WtInternalLink } as WtInternalLink)[0] as WtPageName)[0] as WtText).content
                output[page.title.toLowerCase()] = result
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
    private fun convertToYaml(output: MutableMap<Int, ItemExtras>): Map<String, Map<String, Any>> {
        val nameMap = mutableMapOf<String, Int>()

        fun makeUniqueId(builder: PageCollector) {
            val duplicate = nameMap.containsKey(builder.uid)
            nameMap[builder.uid] = nameMap.getOrDefault(builder.uid, 0) + 1
            if (duplicate) {
                builder.uid = "${builder.uid}_${nameMap[builder.uid]}"
            }
        }

        // Identified id's take priority
        output.filter { it.value.first.idd }.forEach { (_, pair) ->
            val (builder, _) = pair
            makeUniqueId(builder)
        }
        // The rest
        output.filter { !it.value.first.idd }.forEach { (_, pair) ->
            val (builder, _) = pair
            makeUniqueId(builder)
        }

        return output
            .mapNotNull { (id, pair) ->
                val (builder, extras) = pair
                if (builder.uid.startsWith("null", true)) {
                    null
                } else {
                    extras["id"] = id
                    builder.uid to beautify(extras)
                }
            }
            .sortedBy { it.second["id"] as Int }
            .toMap()
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