package rs.dusk.tools.definition.item.pipe.page

import org.apache.commons.io.IOUtils
import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.ItemDefinitionPipeline
import rs.dusk.tools.wiki.model.Wiki
import rs.dusk.tools.wiki.model.WikiPage
import rs.dusk.tools.wiki.scrape.RunescapeWiki.export
import rs.dusk.tools.wiki.scrape.RunescapeWiki.getCategoryLinks
import java.io.File

class ItemPageRS3Wiki : Pipeline.Modifier<ItemDefinitionPipeline.PageCollector> {

    private val itemIds = mutableMapOf<Int, WikiPage>()
    private val pageNames = mutableMapOf<String, WikiPage>()

    private val parenthesesRegexWithSPrefix = "s\\s?(?:\\(.*\\)|[0-9]+)".toRegex()
    private val parenthesesRegex = "\\s(?:\\(.*\\)|[0-9]+)".toRegex()

    init {
        val dir = "rs3-items.xml"
        val file = File(dir)
        if (!file.exists()) {
            println("No existing item dump found.")
            val start = System.currentTimeMillis()
            val pageFile = File("rs3-items-list.txt")
            if (!pageFile.exists()) {
                val list = mutableListOf<String>()
                getCategoryLinks(list, "/w/Category:Items")
                getCategoryLinks(list, "/w/Category:Pets")
                pageFile.writeText(list.joinToString(separator = "\n") { it.removePrefix("/w/") })
                println("Obtained ${list.size} item page names...")
            }
            val list = pageFile.readText()
            val export = export(list)
            IOUtils.copy(export, file.outputStream())
            println("Dumped pages in ${System.currentTimeMillis() - start} ms")
        }

        //Loads
        val currentWiki = Wiki.load(dir)

        // Get pages with explicit ids
        currentWiki.pages.forEach { page ->
            val text = page.revision.text
            if (text.contains("infobox item", true)) {
                applyIds(page, pageNames, itemIds, "infobox item", "id")
            } else if (text.contains("infobox pet", true)) {
                applyIds(page, pageNames, itemIds, "infobox pet", "itemid")
            }
        }
    }

    private fun applyIds(page: WikiPage, names: MutableMap<String, WikiPage>, ids: MutableMap<Int, WikiPage>, infobox: String, itemKey: String) {
        page.getTemplateMaps(infobox).forEach { map ->
            val name = (map["name"] as? String ?: page.title).toLowerCase()
            names[name] = page
            map.forEach { (key, value) ->
                if (key.startsWith(itemKey)) {
                    val string = value as String
                    if (string.contains(",")) {
                        value.split(",").forEach {
                            ids.putIfAbsent(it.trim().toInt(), page)
                        }
                    } else if (value.isNotBlank()) {
                        ids.putIfAbsent(value.toInt(), page)
                    }
                }
            }
        }
    }

    override fun modify(content: ItemDefinitionPipeline.PageCollector): ItemDefinitionPipeline.PageCollector {
        val (id, name, _, rs3, _, _) = content
        if (rs3 == null) {
            val newPage = itemIds[id] ?: pageNames[name.toLowerCase()] ?: pageNames[name.toLowerCase().replace(parenthesesRegex, "")] ?: pageNames[name.toLowerCase().replace(parenthesesRegexWithSPrefix, "")]
            if (newPage != null) {
                return content.copy(rs3Page = newPage, idd = itemIds.containsKey(id))
            }
        }
        return content
    }
}