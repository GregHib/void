package world.gregs.voidps.tools.wiki.scrape

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import java.io.File
import java.net.URLDecoder
import java.nio.charset.Charset
import kotlin.system.measureTimeMillis

/**
 * Dumps item details using the grand exchange api
 */
internal object RunescapeWikiScraper {

    fun getWikiText(url: String): String {
        val doc = Jsoup.connect("https://runescape.wiki$url?action=edit").get()
        val element = doc.select("textarea")
        return element.text()
    }

    fun dumpItemInfoBox(url: String): Map<String, Map<String, String>> {
        val doc = Jsoup.connect("https://runescape.wiki$url").get()
        println("Dumping $url")
        val element = doc.select("table").select(".infobox-item").select("tbody tr")
        val categories = mutableMapOf<String, MutableMap<String, String>>()
        var properties: MutableMap<String, String> = mutableMapOf()
        for (row in element) {
            val th = row.select("th")
            val td = row.select("td")
            if (td.isEmpty()) {
                properties = mutableMapOf()
                categories[th.text()] = properties
                continue
            }
            val key = th.text()
            val value = td.text()
            properties[key] = value
        }
        return categories
    }

    fun dumpItems(category: String) {
        val map = mutableMapOf<String, MutableMap<String, Map<String, Map<String, String>>>>()
        var url: String? = "/w/Category:$category"
        val file = File("Items.json")
        val mapper = ObjectMapper(JsonFactory())
        var count = 1
        while (url != null) {
            val time = measureTimeMillis {
                url = dumpCategoryItems(map, url!!, count++)
                mapper.writeValue(file, map)
            }
            println("Took ${time / 1000}s")
        }
    }

    fun dumpCategory(category: String) {
        val map = mutableMapOf<String, MutableList<Links>>()
        dumpCategoryLinks(map, "/w/Category:$category")
        val file = File("WikiCategory$category.json")
        val mapper = ObjectMapper(JsonFactory())
        mapper.writeValue(file, map)
    }

    interface Links

    internal data class Link(val link: String, val text: String) : Links

    fun dumpCategoryLinks(map: MutableMap<String, MutableList<Links>>, url: String, count: Int = 1) {
        val doc = Jsoup.connect("https://runescape.wiki$url").get()
        println("Dumping page $count $url")
        val element = doc.select("#mw-pages")
        for (ele in element.select("div[class$=mw-category-group]")) {
            val category = ele.select("h3").text()
            val list = map.getOrPut(category) { mutableListOf() }
            for (item in ele.select("ul li a")) {
                val link = item.attr("href")
                val title = item.attr("title")
                list.add(Link(URLDecoder.decode(link, Charset.defaultCharset()), title))
            }
        }
        val nextPage = element.select("a:contains(next page)").attr("href")
        if (nextPage.isNotBlank()) {
            dumpCategoryLinks(map, nextPage, count + 1)
        }
    }

    fun dumpCategoryItems(map: MutableMap<String, MutableMap<String, Map<String, Map<String, String>>>>, url: String, count: Int): String? {
        val doc = Jsoup.connect("https://runescape.wiki$url").get()
        println("Dumping page $count $url")
        val element = doc.select("#mw-pages")
        for (ele in element.select("div[class$=mw-category-group]")) {
            val category = ele.select("h3").text()
            val cat = map.getOrPut(category) { mutableMapOf() }
            val rows = ele.select("ul li a")
            runBlocking {
                cat.putAll(
                    rows.map { item ->
                        async {
                            val link = item.attr("href")
                            val title = item.attr("title")
                            title to dumpItemInfoBox(link)
                        }
                    }.associate {
                        it.await()
                    },
                )
            }
        }
        val nextPage = element.select("a:contains(next page)").attr("href")
        if (nextPage.isNotBlank()) {
            return nextPage
        }
        return null
    }
}
