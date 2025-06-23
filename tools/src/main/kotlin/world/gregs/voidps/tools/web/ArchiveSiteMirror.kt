package world.gregs.voidps.tools.web

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import world.gregs.voidps.tools.web.UrlHandler.convertQuery
import world.gregs.voidps.tools.web.UrlHandler.offset
import world.gregs.voidps.tools.web.UrlHandler.removeDomain
import world.gregs.voidps.tools.web.UrlHandler.removePrefixDomain
import world.gregs.voidps.tools.web.UrlHandler.removeSuffixDomain
import world.gregs.voidps.tools.web.UrlHandler.trimAnchor
import world.gregs.voidps.tools.web.UrlHandler.trimQuery
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Scrapes mirrors from archive-it.org
 */
fun main() {
    // https://wiki.archiveteam.org/index.php?title=Restoring
    val mirrors = listOf(
        SiteMirror("20100121224258", languages = false, knowledgeBase = true, downloads = true, singlePage = true),
        SiteMirror("20120113040119", languages = false, knowledgeBase = true, downloads = true, singlePage = true),
        SiteMirror("20141125214628", languages = false, knowledgeBase = true, downloads = true, singlePage = true),
        SiteMirror("20191114183713", languages = false, knowledgeBase = true, downloads = true, singlePage = true),
        SiteMirror("20090305183759", languages = false, knowledgeBase = true, downloads = true, singlePage = false),
        SiteMirror("20090323174711", languages = false, knowledgeBase = true, downloads = true, singlePage = false),
        SiteMirror("20101206213136", languages = false, knowledgeBase = true, downloads = true, singlePage = false),
        SiteMirror("20110117213247", languages = false, knowledgeBase = true, downloads = true, singlePage = false),
    )
    var running = true
    while (running) {
        for (mirror in mirrors) {
            mirror.next()
        }
    }
}

class SiteMirror(
    date: String,
    private val languages: Boolean,
    private val knowledgeBase: Boolean,
    private val downloads: Boolean,
    private val singlePage: Boolean,
) {

    private var year = date.take(4).toInt()
    private val output = File("./$year-${date.substring(4, 6)}-${date.substring(6, 8)}/")
    private val all: MutableSet<String> = ConcurrentHashMap.newKeySet()
    private val queue = ConcurrentLinkedQueue<Pair<String, String>>()
    private val validUrlRegex = "https?:\\\\?/\\\\?/wayback\\.archive-it\\.org\\\\?/all\\\\?/.*?\\\\?/https?:\\\\?/\\\\?/(?:[a-zA-Z0-9-.]+?)?(?:runescape|jagex).com".toRegex()
    private val testRegex = "(?:https?:)?\\\\?/\\\\?/[-a-zA-Z0-9+&@#\\\\/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]".toRegex()
    private val staticRegex = "\"(/wb-static/[-a-zA-Z0-9+&@#\\\\/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])\"".toRegex()

    init {
        queue("http://wayback.archive-it.org/all/$date/http://www.runescape.com/", force = true)
    }

    fun next() {
        val (req, path) = queue.poll() ?: return
        GlobalScope.launch(Dispatchers.Default) {
            grabPage(req, path)
        }
    }

    private fun shouldSkip(path: String): Boolean {
        val dateIndex = path.indexOf("all/")
        val date = path.substring(dateIndex + 4, dateIndex + 8).toIntOrNull()
        if (date != null && date < year) {
            return true
        }
        if (singlePage && !supportedFileDownloads(trimQuery(trimAnchor(path)))) {
            return true
        }
        if (!languages && (path.contains("l=") || path.contains("set_lang="))) {
            return true
        }
        if (!knowledgeBase && path.contains("kbase")) {
            return true
        }
        if (!downloads && path.contains("downloads_and_wallpapers")) {
            return true
        }
        return false
    }

    private fun queue(archived: String, force: Boolean = false) {
        if (!force && shouldSkip(archived)) {
            return
        }
        val path = getPath(archived) ?: return
        if (!all.contains(path)) {
            queue.add(archived to path)
            all.add(path)
        }
    }

    private fun getPath(source: String): String? = if (validUrlRegex.containsMatchIn(source)) {
        val anchorIndex = source.indexOf("#")
        val anchor = if (anchorIndex >= 0) {
            source.substring(anchorIndex, source.length)
        } else {
            ""
        }
        var path = convertQuery(removeDomain(removePrefixDomain(source.replace(anchor, "").replace(".ws", ".html")), "runescape.com"))
        when {
            path.isBlank() || path == "runescape.com" -> path = "index.html"
            path.endsWith("/") -> path += "index.html"
            !path.endsWith(".html") && !supportedFileDownloads(path) -> {
                path += ".html"
            }
        }
        path
    } else if (!source.contains("http") && !supportedFileDownloads(source)) {
        convertQuery(source)
    } else if (source.contains("/wb-static")) {
        source.substring(source.indexOf("/wb-static") + 1, source.length)
    } else if (source.contains("partner.archive-it.org/static/")) {
        source.substring(source.indexOf("/static/") + 1, source.length)
    } else {
        null
    }

    fun removeDisclaimer(document: Document) {
        for (element in document.select("style").reversed()) {
            if (element.data().contains("disclaim")) {
                element.remove()
            }
        }
        for (element in document.select("wb_div").reversed()) {
            element.remove()
        }
        for (element in document.select("script").reversed()) {
            val data = element.data()
//        if (data.contains("wombat") || data.contains("disclaim") ||data.contains("ait", true) || data.contains("TimeShift")) {
//            element.remove()
//        }
//        val src = element.attr("src")
//        if (src.contains("ait", true)) {
//            element.remove()
//        }
        }
    }

    private fun grabPage(source: String, path: String) {
        val connection = URL(source).openConnection() as HttpURLConnection
        if (connection.responseCode != 200) {
            return
        }
        println("Grab $source $path ${isTextFormat(path)}")
        if (isTextFormat(path)) {
            var data = connection.inputStream.readBytes().toString(Charsets.UTF_8)
            val prefix = removeSuffixDomain(source)
            for (match in testRegex.findAll(data).toList().reversed()) {
                var original = standardise(match.groupValues.last())
                var url = original
                if (url.contains("archive-it.org")) {
                    url = removePrefixDomain(url)
                }

                if (url.contains("runescape.com") || url.contains("jagex.com")) {
                    if (!original.contains("archive-it.org") && prefix.contains("archive-it.org")) {
                        original = "$prefix$original"
                    }

                    val other = getPath(original) ?: continue
                    url = offset(other, path.count { it == '/' })
                    queue(original)
                }

                if (url != original) {
                    data = data.replaceRange(match.range, url)
                }
            }

            for (match in staticRegex.findAll(data).toList().reversed()) {
                val original = standardise(match.groupValues.last())
                queue.add("http://wayback.archive-it.org$original" to original)
                all.add(original)
                data = data.replaceRange(match.range, offset(getPath(original) ?: continue, path.count { it == '/' }))
            }

            if (data.contains(AIT)) {
                getPath(AIT)?.let { url ->
                    data = data.replace(AIT, offset(url, path.count { it == '/' }))
                    queue("https:$AIT")
                }
            }

            val out = File(output, trimAnchor(path))
            out.parentFile.mkdirs()
            if (out.extension == "html") {
                val document = Jsoup.parse(data.replace("charset=iso-8859-1", "charset=utf-8"))
                removeDisclaimer(document)
                if (!data.contains("charset=")) {
                    document.head().appendElement("meta").attr("http-equiv", "Content-Type").attr("content", "text/html;charset=utf-8")
                }
                out.writeText(document.toString(), Charsets.UTF_8)
            } else {
                out.writeText(data)
            }
        } else {
            val out = File(output, trimAnchor(path))
            if (!out.exists()) {
                out.parentFile.mkdirs()
                out.writeBytes(connection.inputStream.readBytes())
            }
        }
    }

    private fun standardise(url: String): String {
        var url = url
        if (url.startsWith("//")) {
            url = "https:$url"
        }
        return url.replace("\\/", "/")
    }

    companion object {
        const val AIT = "//partner.archive-it.org/static/AIT_Analytics.js"
        fun isTextFormat(url: String): Boolean = url.contains(".ws", true) || url.contains(".html", true) || url.contains(".css", true) || url.contains(".js", true)

        val fileTypes = listOf(
            ".exe",
            ".msi",
            ".mp3",
            ".gif",
            ".jpg",
            ".png",
            ".bz2",
            ".zip",
            ".tar",
            ".jar",
            ".ico",
            ".rss",
            ".css",
            ".js",
            ".json",
            ".svg",
            ".dmg",
            ".woff",
            ".woff2",
            ".ttf",
            ".eot",
            ".webp",
            ".webm",
        )
        fun supportedFileDownloads(file: String): Boolean = fileTypes.any { file.contains(it, true) }
    }
}
