package rs.dusk.tools.definition.item.pipe.page

import org.apache.commons.io.IOUtils
import rs.dusk.tools.Pipeline
import rs.dusk.tools.wiki.model.Wiki
import rs.dusk.tools.wiki.model.WikiPage
import rs.dusk.tools.wiki.scrape.RunescapeWiki.export
import rs.dusk.tools.wiki.scrape.RunescapeWiki.getCategoryLinks
import java.io.File

class LivePageCollector(val type: String, categories: List<String>, infoboxes: List<Pair<String, String>>, wiki: String, val searchById: Boolean, val modifier: (PageCollector, WikiPage, Boolean) -> Unit) : Pipeline.Modifier<PageCollector> {

    private val ids = mutableMapOf<Int, WikiPage>()
    private val names = mutableMapOf<String, WikiPage>()
    private val parenthesesRegexWithSPrefix = "s\\s?(?:\\(.*\\)|[0-9]+)".toRegex()
    private val parenthesesRegex = "\\s(?:\\(.*\\)|[0-9]+)".toRegex()

    init {
        val dir = "${type}s.xml"
        val file = File(dir)
        if (!file.exists()) {
            println("No existing $type dump found.")
            val start = System.currentTimeMillis()
            val pageFile = File("${type}s-list.txt")
            if (!pageFile.exists()) {
                val list = mutableListOf<String>()
                categories.forEach {
                    getCategoryLinks(list, "/w/Category:$it", wiki)
                }
                pageFile.writeText(list.joinToString(separator = "\n") { it.removePrefix("/w/") })
                println("Obtained ${list.size} $type page names...")
            }
            val list = pageFile.readText()
            val export = export(list, wiki)
            IOUtils.copy(export, file.outputStream())
            println("Dumped pages in ${System.currentTimeMillis() - start} ms")
        }

        //Loads
        val currentWiki = Wiki.load(dir)

        // Get pages with explicit ids
        currentWiki.pages.forEach { page ->
            val text = page.revision.text
            infoboxes.forEach { (infobox, key) ->
                if (text.contains(infobox, true)) {
                    applyIds(page, names, ids, infobox, key)
                }
            }
        }
    }

    private fun applyIds(page: WikiPage, names: MutableMap<String, WikiPage>, ids: MutableMap<Int, WikiPage>, infobox: String, itemKey: String) {
        page.getTemplateMaps(infobox).forEach { map ->
            val name = (map["name"] as? String ?: page.title).toLowerCase()
            names.putIfAbsent(name, page)
            if (!searchById) {
                return@forEach
            }
            map.forEach { (key, value) ->
                if (key.startsWith(itemKey)) {
                    val string = value as String
                    if (string.contains(",")) {
                        value.split(",").forEach {
                            append(ids, it.trim(), page)
                        }
                    } else {
                        append(ids, value, page)
                    }
                }
            }
        }
    }
    private fun append(ids: MutableMap<Int, WikiPage>, value: String, page: WikiPage) {
        val id = value.toIntOrNull()
        if (id != null) {
            ids.putIfAbsent(id, page)
        } else if (value.isNotBlank() && !value.equals("no", true) && !value.startsWith("hist") && value != "removed") {
            println("Unknown page id ${page.title} '$value'")
        }
    }

    override fun modify(content: PageCollector): PageCollector {
        val id = content.id
        val name = content.name
        val newPage = ids[id] ?: names[name.toLowerCase()] ?: names[name.toLowerCase().replace(parenthesesRegex, "")] ?: names[name.toLowerCase().replace(parenthesesRegexWithSPrefix, "")]
        if (newPage != null) {
            modifier.invoke(content, newPage, ids.containsKey(id))
        }
        return content
    }
}