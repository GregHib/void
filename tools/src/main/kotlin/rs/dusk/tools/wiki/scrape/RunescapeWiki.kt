package rs.dusk.tools.wiki.scrape

import org.jsoup.Jsoup
import org.jsoup.nodes.FormElement
import java.io.BufferedInputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object RunescapeWiki {

    fun export(pages: String, wiki: String = "runescape.wiki"): BufferedInputStream {
        val doc = Jsoup.connect("https://$wiki/w/Special:Export").get()
        val form: FormElement = doc.select("form").first { it.attr("action") == "/w/Special:Export" } as FormElement
        val text = form.select("textarea").first { it.attr("name") == "pages" }
        text.`val`(pages)
        val current = form.select("input").first { it.attr("name") == "curonly" }
        val templates = form.select("input").first { it.attr("name") == "templates" }
        val download = form.select("input").first { it.attr("name") == "wpDownload" }

        current.attr("checked", "checked")
        templates.removeAttr("checked")
        download.removeAttr("checked")

        return form.submit().maxBodySize(0).timeout(0).execute().bodyStream()
    }

    fun getCategoryLinks(list: MutableList<String>, url: String, wiki: String = "runescape.wiki", count: Int = 1) {
        val doc = Jsoup.connect("https://$wiki$url").get()
        println("Dumping page $count $url")
        val element = doc.select("#mw-pages")
        for (ele in element.select("div[class$=mw-category-group]")) {
//            val category = ele.select("h3").text()
            for (item in ele.select("ul li a")) {
                val link = item.attr("href")
//                val title = item.attr("title")
                list.add(URLDecoder.decode(link, StandardCharsets.UTF_8))
            }
        }
        val nextPage = element.select("a:contains(next page)").attr("href")
        if (nextPage.isNotBlank()) {
            getCategoryLinks(list, nextPage, wiki, count + 1)
        }
    }

}