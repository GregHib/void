package world.gregs.voidps.tools.definition.npc

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
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
import world.gregs.voidps.tools.property
import world.gregs.voidps.tools.wiki.model.Wiki
import world.gregs.yaml.Yaml
import world.gregs.yaml.write.YamlWriterConfiguration
import java.io.File
import java.time.LocalDate
import java.time.Month
import java.util.concurrent.TimeUnit

object NPCDefinitionPipeline {
    private const val DEBUG_ID = -1

    @JvmStatic
    fun main(args: Array<String>) {
        val rs2Wiki = Wiki.load("${System.getProperty("user.home")}\\Downloads\\runescape_pages_full\\runescapewiki-latest-pages-articles-2011-01-31.xml")
        val start = System.currentTimeMillis()
        val cache: Cache = CacheDelegate(property("storage.cache.path"))
        val decoder = NPCDecoder(true).load(cache)
        val pages = getPages(decoder, rs2Wiki)
        val output = buildNPCExtras(decoder, pages)
        val map = convertToYaml(output)
        val yaml = Yaml()
        val config = YamlWriterConfiguration(forceQuoteStrings = true)
        val file = File("npcs.yml")
        yaml.save(file.path, map, config)
        val contents = "# Don't edit; apply changes to the NPCDefinitionPipeline tool's NPCManualChanges class instead.\n${file.readText()}"
        file.writeText(contents)
        println("${output.size} npc definitions written to ${file.path} in ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start)}s")
    }

    /**
     * Collects a rs2 and a rs3 page for each [decoder] item id.
     */
    private fun getPages(decoder: Array<NPCDefinition>, rs2Wiki: Wiki): MutableMap<Int, PageCollector> {
        val pipeline = Pipeline<PageCollector>().apply {
            add(LivePageCollector(
                "osrs-npc",
                listOf("Monsters", "Non-player_characters"),
                listOf(
                    "infobox monster" to "id",
                    "infobox npc" to "id"
                ),
                "oldschool.runescape.wiki",
                false// OSRS ids are scrambled :(
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

        for (id in decoder.indices) {
            if (DEBUG_ID >= 0 && id != DEBUG_ID) {
                continue
            }
            val def = decoder.getOrNull(id) ?: continue
            val processed = pipeline.modify(PageCollector(id, def.name))
            val (_, name, page, _, rs3, _) = processed
            if (page == null && rs3 == null && name != "null") {
                incomplete.add(processed)
            } else if (page != null || rs3 != null) {
                pages[id] = processed
            }
        }

        collectUnknownPages("osrs-npc", incomplete, null, pages, listOf("infobox monster", "infobox npc")) { id, page ->
            (pages[id] ?: PageCollector(id, decoder[id].name)).apply {
                osrs = page
            }
        }
        collectUnknownPages("rs3-npc", incomplete, null, pages, listOf("infobox monster", "infobox npc")) { id, page ->
            (pages[id] ?: PageCollector(id, decoder[id].name)).apply {
                rs3 = page
            }
        }
        return pages
    }

    private val revision = LocalDate.of(2011, Month.OCTOBER, 4)

    private fun buildNPCExtras(
        decoder: Array<NPCDefinition>,
        pages: MutableMap<Int, PageCollector>
    ): MutableMap<Int, Extras> {
        val output = mutableMapOf<Int, Extras>()
        val infoboxes = listOf("infobox monster", "infobox npc", "infobox non-player character")
        val pipeline = Pipeline<Extras>().apply {
            add(InfoBoxNPC(revision, infoboxes))
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
            add(NPCManualChanges())
            add(NPCDefaults())
        }
        return postProcess.modify(output)
    }
}