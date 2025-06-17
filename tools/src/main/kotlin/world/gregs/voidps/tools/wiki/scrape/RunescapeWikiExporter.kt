package world.gregs.voidps.tools.wiki.scrape

import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object RunescapeWikiExporter {

    @JvmStatic
    fun main(args: Array<String>) {
        val wiki = "oldschool.runescape.wiki"
        val timestamp = DateTimeFormatter
            .ofPattern("yyyy-MM-dd")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())
        val file = File("./${wiki.replace(".", "-")}-$timestamp.xml.bz2")
        if (file.exists()) {
            println("File already exists")
            return
        }
        val start = System.currentTimeMillis()
        val pages = mutableSetOf<String>()
        val pageFile = File("$wiki.pages")
        if (pageFile.exists()) {
            pages.addAll(pageFile.readLines())
        } else {
            val namespaces = listOf("(Main)", "Template", "Help", "Calculator", "Map", "Transcript", "Update", "Module", "User")
            for (namespace in namespaces) {
                val links = RunescapeWiki.getAllPages(namespace, wiki, hideRedirects = false)
                println("Collected ${links.size} $namespace namespace pages.")
                pages.addAll(links)
            }
            val categories = listOf("Mathematical_templates", "Calculator_templates")
            for (category in categories) {
                val links = RunescapeWiki.getCategoryLinks("/w/Category:$category", wiki)
                println("Collected ${links.size} $category category pages.")
                pages.addAll(links)
            }
            pageFile.writeText(pages.joinToString(separator = "\n"))
        }
        println("${pages.size} pages found.")
        RunescapeWiki.exportToFile(file, pages.toList(), wiki, currentOnly = true, includeTemplates = true, batchSize = 5_000)
        println("${pages.size} pages saved in ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start)}s")
    }
}
