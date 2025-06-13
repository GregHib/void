package world.gregs.voidps.tools.wiki.scrape

import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import org.jsoup.Jsoup
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import world.gregs.voidps.engine.client.ui.chat.suffixIfNot
import java.io.File
import java.net.URI

object WebsiteScraper {

    @JvmStatic
    fun main(args: Array<String>) {
        val folder = File("./temp/chisel/dialogue/")
        val contentPages = (200_000..226_165).filter { !folder.resolve("content/$it.html").exists() }.map { "content/$it" }
//        val pages = Jsoup.parse(File("./temp/chisel/dialogue/index.html").readText())
//            .select("a")
//            .map { it.attr("href") }
//            .filter { !folder.resolve("$it.html").exists() }
        downloadThreaded(folder, "https://chisel.weirdgloop.org/dialogue/", contentPages, 5_000)
    }

    @Suppress("OPT_IN_USAGE")
    private fun downloadThreaded(folder: File, url: String, pages: List<String>, chunkSize: Int) {
        runBlocking {
            supervisorScope {
                val drivers = mutableListOf<FirefoxDriver>()
                val chunks = pages.chunked(chunkSize)
                println("${pages.size} pages to download in ${chunks.size} batches")
                val context = newFixedThreadPoolContext(chunks.size, "driver")
                val options = FirefoxOptions()
                for ((index, chunk) in chunks.withIndex()) {
                    launch(context) {
                        val driver = FirefoxDriver(options)
                        drivers.add(driver)
                        try {
                            println("Start $index")
                            for (page in chunk) {
                                val file = folder.resolve("$page.html")
                                if (!file.exists()) {
                                    driver.get("$url$page")
                                    val source = driver.pageSource
                                    file.parentFile.mkdirs()
                                    file.writeText(source)
                                    if (++total % 100 == 0) {
                                        println("$total pages downloaded.")
                                    }
                                }
                            }
                        } finally {
                            driver.close()
                        }
                    }
                    Runtime.getRuntime().addShutdownHook(
                        Thread {
                            for (driver in drivers) {
                                driver.close()
                            }
                        },
                    )
                }
            }
        }
    }

    private const val MAX_DEPTH = -1

    @Volatile
    private var total = 0

    private fun downloadOptimised(folder: File, uri: URI, driver: FirefoxDriver? = null, visited: MutableSet<URI> = mutableSetOf(), depth: Int = 0) {
        if (!visited.add(uri) || depth < MAX_DEPTH) {
            return
        }
        val file = if (uri.path.endsWith("/")) {
            folder.resolve(uri.path.trim('/')).resolve("index.html")
        } else {
            folder.resolve(uri.path.trim('/').suffixIfNot(".html"))
        }
        if (file.exists()) {
            return
        }

        driver!!.get(uri.toString())
        driver.currentUrl
        val html = driver.pageSource
        file.parentFile.mkdirs()
        file.writeText(html)
        if (++total % 100 == 0) {
            println("$total pages downloaded.")
        }

        val doc = Jsoup.parse(html)
        val references = doc.select("a").map { it.attr("href") }
        for (href in references) {
            val resolved = uri.resolve(href.removeSuffix(".html"))
            downloadOptimised(folder, resolved, driver, visited, depth + 1)
        }
    }

    private fun download(folder: File, uri: URI, driver: FirefoxDriver? = null, visited: MutableSet<URI> = mutableSetOf(), depth: Int = 0) {
        if (!visited.add(uri) || depth < MAX_DEPTH) {
            return
        }
        val file = if (uri.path.endsWith("/")) {
            folder.resolve(uri.path.trim('/')).resolve("index.html")
        } else {
            folder.resolve(uri.path.trim('/').suffixIfNot(".html"))
        }
        println("$uri -> $file")
        var html = if (file.exists()) {
            file.readText()
        } else {
            driver!!.get(uri.toString())
            val source = driver.pageSource
            file.parentFile.mkdirs()
            file.writeText(source)
            if (++total % 100 == 0) {
                println("$total pages downloaded.")
            }
            source
        }
        val doc = Jsoup.parse(html)
        val references = doc.select("a").map { it.attr("href") }
        for (href in references) {
            val processed = href.endsWith(".html")
            if (!href.endsWith("/") && !processed) {
                html = html.replace("href=\"${href}\"", "href=\"$href.html\"")
                file.writeText(html)
            }
            val resolved = uri.resolve(href.removeSuffix(".html"))
            download(folder, resolved, driver, visited, depth + 1)
        }
    }
}
