package world.gregs.voidps.tools.wiki.scrape

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URL

/**
 * Dumps item details using the grand exchange api
 */
object GrandExchangeDetailDumper {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val file = File("./GrandExchangeDetails.json")
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText("[")
        FileOutputStream(file, true).use {
            it.write("[".toByteArray())
            for (id in 0 until 22323) {
                try {
                    val apiResponse = URL("https://secure.runescape.com/m=itemdb_rs/api/catalogue/detail.json?item=$id").readText()
                    println("Dumping $id")
                    file.appendText("$apiResponse,")
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
                delay(100L)
            }
            it.write("]".toByteArray())
        }
        println("Done")
    }
}
