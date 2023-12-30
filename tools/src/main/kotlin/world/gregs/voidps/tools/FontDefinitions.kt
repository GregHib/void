package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.Index.FONT_METRICS
import world.gregs.voidps.cache.definition.decoder.FontDecoder

object FontDefinitions {

    @JvmStatic
    fun main(args: Array<String>) {
        val cache: Cache = CacheDelegate(property("cachePath"))
        val definitions = FontDecoder().loadCache(cache)
        val font = definitions[cache.getArchiveId(FONT_METRICS, "q8_full")]
        println(font.textWidth("This is a string"))
        println(font.splitLines("Another 'archaeologist'. I'm not going to let you plunder my master's tomb you know.", 380))
    }
}