package rs.dusk.tools.definition.item.pipe.page

import rs.dusk.tools.Pipeline
import rs.dusk.tools.definition.item.ItemDefinitionPipeline
import rs.dusk.tools.wiki.model.Wiki
import rs.dusk.tools.wiki.model.WikiPage

class ItemPageWiki(val wiki: Wiki) : Pipeline.Modifier<ItemDefinitionPipeline.PageCollector> {

    private val pageNames = mutableMapOf<String, WikiPage>()

    init {
        wiki.pages.forEach { page ->
            val text = page.revision.text
            when {
                text.contains("infobox item", true) -> {
                    appendName("infobox item", page)
                }
                text.contains("infobox construction", true) -> {
                    appendName("infobox construction", page)
                }
            }
        }
    }

    private fun appendName(title: String, page: WikiPage) {
        val map = page.getTemplateMap(title) ?: return
        val name = (map["name"] as? String)?.toLowerCase()
        val redirectedPage = if (page.redirected) page.getRedirect(wiki) ?: page else page
        if(name != null) {
            pageNames[name] = redirectedPage
        }
        pageNames[page.title.toLowerCase()] = redirectedPage
        pageNames[redirectedPage.title.toLowerCase()] = redirectedPage
    }

    fun getPage(rs3: WikiPage?, rs2: WikiPage?, name: String): WikiPage? {
        if(rs3 != null) {
            val page = pageNames[rs3.title.toLowerCase()]
            if (page != null) {
                return page
            }
        }
        if(rs2 != null) {
            val page = pageNames[rs2.title.toLowerCase()]
            if (page != null) {
                return page
            }
        }
        val page = pageNames[name.toLowerCase()]
        if (page != null) {
            return page
        }
        return wiki.getExactPageOrNull(name)
    }

    override fun modify(content: ItemDefinitionPipeline.PageCollector): ItemDefinitionPipeline.PageCollector {
        val (_, name, page, rs3, _) = content
        val newPage = getPage(rs3, page, name)
        if (newPage != null) {
            if(newPage.redirected) {
                val redirected = newPage.getRedirect(wiki)
                if(redirected != null) {
                    return content.copy(page = redirected)
                }
            }
            return content.copy(page = newPage)
        }
        return content
    }
}