package world.gregs.voidps.tools.definition.npc

import org.koin.core.context.startKoin
import org.koin.fileProperties
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule
import world.gregs.voidps.engine.data.file.FileStorage
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras
import world.gregs.voidps.tools.definition.item.ItemDefinitionPipeline.collectUnknownPages
import world.gregs.voidps.tools.definition.item.ItemDefinitionPipeline.convertToYaml
import world.gregs.voidps.tools.definition.item.pipe.page.LivePageCollector
import world.gregs.voidps.tools.definition.item.pipe.page.OfflinePageCollector
import world.gregs.voidps.tools.definition.item.pipe.page.PageCollector
import world.gregs.voidps.tools.definition.item.pipe.page.UniqueIdentifiers
import world.gregs.voidps.tools.definition.npc.pipe.wiki.InfoBoxNPC
import world.gregs.voidps.tools.definition.npc.pipe.wiki.NPCDefaults
import world.gregs.voidps.tools.definition.npc.pipe.wiki.NPCManualChanges
import world.gregs.voidps.tools.wiki.model.Wiki
import java.io.File
import java.time.LocalDate
import java.time.Month
import java.util.concurrent.TimeUnit

object NPCDefinitionPipeline {
    private const val debugId = -1

    @JvmStatic
    fun main(args: Array<String>) {
        val rs2Wiki = Wiki.load("${System.getProperty("user.home")}\\Downloads\\runescape_pages_full\\runescapewiki-latest-pages-articles-2011-01-31.xml")
        val start = System.currentTimeMillis()
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val decoder = NPCDecoder(koin.get(), true)
        val pages = getPages(decoder, rs2Wiki)
        val output = buildNPCExtras(decoder, pages)
        val map = convertToYaml(output)
        val storage = FileStorage(true)
        val file = File("npcs.yml")
        storage.save(file, map)
        val contents = "# Don't edit; apply changes to the NPCDefinitionPipeline tool's NPCManualChanges class instead.\n${file.readText()}"
        file.writeText(contents)
        println("${output.size} npc definitions written to ${file.path} in ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start)}s")
    }

    /**
     * Collects a rs2 and a rs3 page for each [decoder] item id.
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
                "oldschool.runescape.wiki",
                false// OSRS id's are scrambled :(
            ) { content, page, _ ->
                content.osrs = page
            })
            add(LivePageCollector(
                "rs3-npc",
                listOf("Bestiary", "Non-player_characters"),
                listOf(
                    "infobox monster" to "id",
                    "infobox npc" to "id",
                    "infobox non-player character" to "id"
                ),
                "runescape.wiki",
                true
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

        repeat(decoder.last) { id ->
            if(debugId >= 0 && id != debugId) {
                return@repeat
            }
            val def = decoder.getOrNull(id) ?: return@repeat
            val processed = pipeline.modify(PageCollector(id, def.name))
            val (_, name, page, _, rs3, _) = processed
            if (page == null && rs3 == null && name != "null") {
                incomplete.add(processed)
            } else if (page != null || rs3 != null) {
                pages[id] = processed
            }
        }

        collectUnknownPages("osrs-npc", incomplete, null, pages, listOf("infobox monster", "infobox npc")) { id, page ->
            (pages[id] ?: PageCollector(id, decoder.get(id).name)).apply {
                osrs = page
            }
        }
        collectUnknownPages("rs3-npc", incomplete, null, pages, listOf("infobox monster", "infobox npc")) { id, page ->
            (pages[id] ?: PageCollector(id, decoder.get(id).name)).apply {
                rs3 = page
            }
        }
        return pages
    }

    private val revision = LocalDate.of(2011, Month.OCTOBER, 4)

    private fun buildNPCExtras(
        decoder: NPCDecoder,
        pages: MutableMap<Int, PageCollector>
    ): MutableMap<Int, Extras> {
        val output = mutableMapOf<Int, Extras>()
        val infoboxes = listOf("infobox monster", "infobox npc", "infobox non-player character")
        val pipeline = Pipeline<Extras>().apply {
            add(InfoBoxNPC(revision, infoboxes))
        }
        repeat(decoder.last) { id ->
            if (debugId >= 0 && id != debugId) {
                return@repeat
            }
            val def = decoder.getOrNull(id) ?: return@repeat
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
            add(NPCManualChanges())
            add(NPCDefaults())
        }
        return postProcess.modify(output)
    }
}