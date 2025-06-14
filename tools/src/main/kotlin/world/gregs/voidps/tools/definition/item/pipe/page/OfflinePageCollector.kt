package world.gregs.voidps.tools.definition.item.pipe.page

import world.gregs.voidps.tools.Pipeline
import world.gregs.voidps.tools.wiki.model.Wiki
import world.gregs.voidps.tools.wiki.model.WikiPage

class OfflinePageCollector(val wiki: Wiki, infoboxes: List<String>, val function: (PageCollector, WikiPage) -> Unit) : Pipeline.Modifier<PageCollector> {

    private val pageNames = mutableMapOf<String, WikiPage>()

    init {
        wiki.pages.forEach { page ->
            val text = page.revision.text
            infoboxes.forEach {
                if (text.contains(it, true)) {
                    appendName(it, page)
                }
            }
        }
    }

    private fun appendName(title: String, page: WikiPage) {
        val map = page.getTemplateMap(title) ?: return
        val name = (map["name"] as? String)?.lowercase()
        val redirectedPage = if (page.redirected) page.getRedirect(wiki) ?: page else page
        if (name != null) {
            pageNames.putIfAbsent(name, redirectedPage)
        }
        pageNames.putIfAbsent(page.title.lowercase(), redirectedPage)
        pageNames.putIfAbsent(redirectedPage.title.lowercase(), redirectedPage)
    }

    fun getPage(rs3: WikiPage?, rs2: WikiPage?, name: String): WikiPage? {
        if (rs3 != null) {
            val page = pageNames[rs3.title.lowercase()]
            if (page != null) {
                return page
            }
        }
        if (rs2 != null) {
            val page = pageNames[rs2.title.lowercase()]
            if (page != null) {
                return page
            }
        }
        val page = pageNames[name.lowercase()]
        if (page != null) {
            return page
        }
        return wiki.getExactPageOrNull(name)
    }

    override fun modify(content: PageCollector): PageCollector {
        val (_, name, page, _, rs3, _) = content
        val newPage = getPage(rs3, page, name)
        if (newPage != null) {
            if (newPage.redirected) {
                val redirected = newPage.getRedirect(wiki)
                if (redirected != null) {
                    function.invoke(content, redirected)
                    return content.copy(rs2 = redirected)
                }
            }
            function.invoke(content, newPage)
            return content
        }
        return content
    }
}
