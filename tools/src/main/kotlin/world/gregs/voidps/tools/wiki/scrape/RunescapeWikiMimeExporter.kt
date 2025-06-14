package world.gregs.voidps.tools.wiki.scrape

import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Downloads files from wiki to folder. Find list of types at
 * https://runescape.wiki/w/Special:MediaStatistics
 */
object RunescapeWikiMimeExporter {

    @JvmStatic
    fun main(args: Array<String>) {
        val wiki = "oldschool.runescape.wiki"
        val start = System.currentTimeMillis()
        val folder = File("./osrs-mp4/")
        folder.mkdirs()
        val type = "video/mp4"
        val count = RunescapeWiki.downloadMimeFiles(folder, type, wiki)
        println("$count $type files saved in ${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start)}s")
    }
}
