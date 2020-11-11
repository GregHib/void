package rs.dusk.tools.definition.npc

import org.koin.core.context.startKoin
import rs.dusk.cache.definition.decoder.NPCDecoder
import rs.dusk.engine.client.cacheDefinitionModule
import rs.dusk.engine.client.cacheModule
import rs.dusk.engine.data.file.FileLoader
import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.Extras
import rs.dusk.tools.definition.item.ItemDefinitionPipeline.collectUnknownPages
import rs.dusk.tools.definition.item.ItemDefinitionPipeline.convertToYaml
import rs.dusk.tools.definition.item.pipe.page.LivePageCollector
import rs.dusk.tools.definition.item.pipe.page.OfflinePageCollector
import rs.dusk.tools.definition.item.pipe.page.PageCollector
import rs.dusk.tools.definition.npc.pipe.wiki.InfoBoxMonster
import rs.dusk.tools.definition.npc.pipe.wiki.NPCDefaults
import rs.dusk.tools.wiki.model.Wiki
import java.io.File
import java.util.concurrent.TimeUnit

object NPCDefinitionPipeline {
    private const val debugId = -1

    @JvmStatic
    fun main(args: Array<String>) {
        val rs2Wiki = Wiki.load("${System.getProperty("user.home")}\\Downloads\\runescapewiki-latest-pages-articles-2011-08-14.xml")
        val start = System.currentTimeMillis()
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = NPCDecoder(koin.get(), true)
        val pages = getPages(decoder, rs2Wiki)
        val output = buildNPCExtras(decoder, pages)
        val map = convertToYaml(output)
        val loader = FileLoader(true)
        val file = File("npc-definition-extras.yml")
        loader.save(file, map)
        val contents = "# Don't edit; apply changes to the NPCDefinitionPipeline tool's NPCManualChanges class instead.\n${file.readText()}"
        file.writeText(contents)
        println("${output.size} npc definitions written to ${file.path} in ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start)}s")
    }

    /**
     * Collects a rs2 and an rs3 page for each [decoder] item id.
     */
    private fun getPages(decoder: NPCDecoder, rs2Wiki: Wiki): MutableMap<Int, PageCollector> {
        val pipeline = Pipeline<PageCollector>().apply {
            add(LivePageCollector(
                "osrs-npc",
                listOf("Monsters", "Non-player_characters"),
                listOf(
                    "infobox monster" to "id",
                    "infobox npc" to "id"
                ),
                "oldschool.runescape.wiki"
            ) { content, page, idd ->
                if(!idd) {// OSRS id's are scrambled :(
                    content.osrs = page
                }
            })
            add(LivePageCollector(
                "rs3-npc",
                listOf("Bestiary", "Non-player_characters"),
                listOf(
                    "infobox monster" to "id",
                    "infobox npc" to "id"
                ),
                "runescape.wiki"
            ) { content, page, idd ->
                content.rs3 = page
                content.rs3Idd = idd
            })
            add(OfflinePageCollector(rs2Wiki, listOf("infobox monster", "infobox npc")) { content, page ->
                content.rs2 = page
            })
        }

        val pages = mutableMapOf<Int, PageCollector>()
        val incomplete = mutableListOf<PageCollector>()

        repeat(decoder.size) { id ->
//            if(debugId > 0 && id != debugId) {
//                return@repeat
//            }
            val def = decoder.getOrNull(id) ?: return@repeat
            val processed = pipeline.modify(PageCollector(id, def.name))
            val (_, name, page, _, rs3, _) = processed
            if (page == null && rs3 == null && name != "null") {
                incomplete.add(processed)
            } else if (page != null || rs3 != null) {
                pages[id] = processed
            }
        }

        collectUnknownPages("osrs", incomplete, null, pages, listOf("infobox monster", "infobox npc")) { id, page ->
            if (pages.containsKey(id)) {
                pages[id]!!.copy(osrs = page)
            } else {
                PageCollector(id, decoder.get(id).name, osrs = page)
            }
        }
        collectUnknownPages("rs3", incomplete, null, pages, listOf("infobox monster", "infobox npc")) { id, page ->
            if (pages.containsKey(id)) {
                pages[id]!!.copy(rs3 = page)
            } else {
                PageCollector(id, decoder.get(id).name, rs3 = page)
            }
        }
        return pages
    }

    private fun buildNPCExtras(
        decoder: NPCDecoder,
        pages: MutableMap<Int, PageCollector>
    ): MutableMap<Int, Extras> {
        val output = mutableMapOf<Int, Extras>()
        val pipeline = Pipeline<Extras>().apply {
            add(InfoBoxMonster())
            add(NPCDefaults())
        }
        repeat(decoder.size) { id ->
            if (debugId > 0 && id != debugId) {
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
}