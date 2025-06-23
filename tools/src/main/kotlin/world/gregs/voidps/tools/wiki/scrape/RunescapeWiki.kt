package world.gregs.voidps.tools.wiki.scrape

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.jsoup.Jsoup
import org.jsoup.nodes.FormElement
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object RunescapeWiki {

    fun export(pages: String, wiki: String = "runescape.wiki", currentOnly: Boolean = true, includeTemplates: Boolean = false): BufferedInputStream {
        val doc = Jsoup.connect("https://$wiki/w/Special:Export")
            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
            .get()
        val form: FormElement = doc.select("form").first { it.attr("action") == "/w/Special:Export" } as FormElement
        val text = form.select("textarea").first { it.attr("name") == "pages" }
        text.`val`(pages)
        val current = form.select("input").first { it.attr("name") == "curonly" }
        val templates = form.select("input").first { it.attr("name") == "templates" }
        val download = form.select("input").first { it.attr("name") == "wpDownload" }

        if (currentOnly) {
            current.attr("checked", "checked")
        } else {
            current.removeAttr("checked")
        }
        if (includeTemplates) {
            templates.attr("checked", "checked")
        } else {
            templates.removeAttr("checked")
        }
        download.removeAttr("checked")
        return form.submit().maxBodySize(0).timeout(0).execute().bodyStream()
    }

    fun downloadMimeFiles(folder: File, type: String, wiki: String = "runescape.wiki", offset: Int = 0): Int {
        val doc = Jsoup.connect("https://$wiki/w/Special:MIMESearch?limit=500&mime=$type&offset=$offset").get()
        val elements = doc.select("a[class$=internal]")
        runBlocking {
            elements.chunked(50).map { list ->
                list.map { ele ->
                    GlobalScope.async {
                        val file = folder.resolve(ele.attr("title"))
                        if (file.exists()) {
                            return@async
                        }
                        val response = Jsoup.connect("https://$wiki/${ele.attr("href")}").ignoreContentType(true).execute()
                        if (response.statusCode() != 200) {
                            return@async
                        }
                        file.writeBytes(response.bodyAsBytes())
                        println("Downloaded ${ele.attr("title")}.")
                    }
                }.map { it.await() }
            }
        }
        val next = doc.select("a[class$=mw-nextlink]").firstOrNull()
        if (next != null) {
            downloadMimeFiles(folder, type, wiki, offset + 500)
        }
        return folder.list()?.size ?: 0
    }

    fun exportToFile(file: File, pages: List<String>, wiki: String = "runescape.wiki", currentOnly: Boolean = true, includeTemplates: Boolean = false, batchSize: Int = 5_000) {
        val parent = file.parentFile
        val files = runBlocking {
            pages.chunked(batchSize).mapIndexed { index, list ->
                GlobalScope.async {
                    val temp = parent.resolve("${file.nameWithoutExtension}_temp_$index.xml")
                    if (!temp.exists()) {
                        println("Downloading ${file.nameWithoutExtension} part $index")
                        temp.writeBytes(export(list.joinToString(separator = "\n"), wiki, currentOnly, includeTemplates).readAllBytes())
                    }
                    temp
                }
            }.map { it.await() }
        }
        println("Combining files.")
        var first = true
        val compressStream = BZip2CompressorOutputStream(BufferedOutputStream(file.outputStream()))
        for (temp in files) {
            val text = temp.readText()
            val start = text.indexOf("<page>")
            val end = text.lastIndexOf("</page>")
            if (start == -1 || end == -1) {
                continue
            }
            if (first) {
                compressStream.write(text.substring(0, end + 7).toByteArray())
                first = false
            } else {
                compressStream.write("\n  ".toByteArray())
                compressStream.write(text.substring(start, end + 7).toByteArray())
            }
        }
        compressStream.write("\n</mediawiki>\n".toByteArray())
        compressStream.close()
        println("Cleaning up temp files.")
        for (temp in files) {
            temp.delete()
        }
    }

    fun getCategoryLinks(url: String, wiki: String = "runescape.wiki", list: MutableList<String> = mutableListOf()): List<String> {
        val connect = Jsoup.connect("https://$wiki/$url")
        val response = connect.execute()
        if (response.statusCode() != 200) {
            println("Page not found: $wiki $url")
            return list
        }
        val doc = response.parse()
        val element = doc.select("#mw-pages")
        for (ele in element.select("div[class$=mw-category-group]")) {
            for (item in ele.select("ul li a")) {
                val link = item.attr("href")
                list.add(URLDecoder.decode(link, StandardCharsets.UTF_8.name()))
            }
        }
        val nextPage = element.select("a:contains(next page)").attr("href")
        if (nextPage.isNotBlank()) {
            getCategoryLinks(nextPage, wiki, list)
        }
        return list
    }

    fun getCategories(wiki: String = "runescape.wiki", url: String = "/w/Special:Categories?limit=500", list: MutableList<String> = mutableListOf()): List<String> {
        val doc = Jsoup.connect("https://${wiki}$url").get()
        val elements = doc.select("div[class$=mw-spcontent] ul li")
        for (ele in elements) {
            val link = ele.select("a")
            list.add(link.attr("title"))
        }
        val next = doc.select("a[class$=mw-nextlink]").firstOrNull()
        if (next != null) {
            getCategories(wiki, next.attr("href"), list)
        }
        return list
    }

    fun getCategoriesLinks(wiki: String = "runescape.wiki", url: String = "/w/Special:Categories?limit=500", list: MutableList<String> = mutableListOf()): List<String> {
        val doc = Jsoup.connect("https://${wiki}$url").get()
        val elements = doc.select("div[class$=mw-spcontent] ul li")
        for (ele in elements) {
            val link = ele.select("a")
            list.add(link.attr("href"))
        }
        val next = doc.select("a[class$=mw-nextlink]").firstOrNull()
        if (next != null) {
            getCategories(wiki, next.attr("href"), list)
        }
        return list
    }

    fun getAllPages(namespace: String, wiki: String = "runescape.wiki", hideRedirects: Boolean = true): List<String> {
        val namespaces = getNamespaces(wiki)
        val namespaceId = namespaces[namespace]!!
        val url = "/w/Special:AllPages?from=&to=&namespace=$namespaceId${if (hideRedirects) "&hideredirects=1" else ""}"
        return getAllPages(mutableListOf(), wiki, url)
    }

    private fun getAllPages(list: MutableList<String>, wiki: String, url: String): List<String> {
        val doc = Jsoup.connect("https://${wiki}$url").get()
        val elements = doc.select("div[class$=mw-allpages-body] a")
        for (ele in elements) {
            list.add(ele.attr("title"))
        }
        val next = doc.select("div[class$=mw-allpages-nav] a").firstOrNull { it.text().startsWith("Next page") }
        if (next != null) {
            getAllPages(list, wiki, next.attr("href"))
        }
        return list
    }

    private fun getNamespaces(wiki: String): Map<String, Int> {
        val doc = Jsoup.connect("https://$wiki/w/Special:AllPages").get()
        val options = doc.select("div[class$=mw-widget-namespaceInputWidget] option")
        return options.associate { it.text() to it.attr("value").toInt() }
    }
}
