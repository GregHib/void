package world.gregs.voidps.tools.definition.item.pipe.extra.wiki

import org.apache.commons.io.IOUtils
import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.definition.item.Extras
import world.gregs.voidps.tools.wiki.model.Wiki
import world.gregs.voidps.tools.wiki.scrape.RunescapeWiki.export
import world.gregs.voidps.tools.wiki.scrape.RunescapeWiki.getCategoryLinks
import java.io.File

class ItemExchangeLimits : Pipeline.Modifier<Extras> {

    private val idLimits = mutableMapOf<Int, Int>()
    private val nameLimits = mutableMapOf<String, Int>()

    init {
        val dir = "rs3-exchange.xml"
        val file = File(dir)
        if (!file.exists()) {
            println("No existing item dump found.")
            val start = System.currentTimeMillis()
            val pageFile = File("rs3-exchange-list.txt")
            if (!pageFile.exists()) {
                val list = getCategoryLinks("/w/Category:Grand_Exchange_items")
                pageFile.writeText(list.joinToString(separator = "\n") { "Module:Exchange/${it.removePrefix("/w/")}" })
                println("Obtained ${list.size} exchange page names...")
            }
            val list = pageFile.readText()
            val export = export(list)
            IOUtils.copy(export, file.outputStream())
            println("Dumped pages in ${System.currentTimeMillis() - start} ms")
        }

        // Loads
        val currentWiki = Wiki.load(dir)

        // Get pages with explicit ids
        currentWiki.pages.forEach { page ->
            val text = page.revision.text.removePrefix("return {").removeSuffix("}")
            if (text.contains(",", true)) {
                val rows = text.split(",")
                val data = rows.associate { row ->
                    val parts = row.trim().split("=")
                    parts.first().trim() to parts.last().trim()
                }
                val id = data.getValue("itemId").toInt()
                val limit = data["limit"]?.toIntOrNull() ?: 0
                idLimits[id] = limit
                nameLimits[page.title.removePrefix("Module:Exchange/").lowercase()] = limit
            }
        }
    }

    override fun modify(content: Extras): Extras {
        val (builder, extras) = content
        val (id, _, page, _, rs3, _, _) = builder
        val limit = idLimits[id] ?: nameLimits[page?.title?.lowercase() ?: rs3?.title?.lowercase()] ?: return content
        extras["limit"] = limit
        return content
    }
}
