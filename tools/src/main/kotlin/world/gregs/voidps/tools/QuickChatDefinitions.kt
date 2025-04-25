package world.gregs.voidps.tools

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.QuickChatOptionDecoder
import world.gregs.voidps.cache.definition.decoder.QuickChatPhraseDecoder
import world.gregs.voidps.engine.data.Settings

object QuickChatDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val options = QuickChatOptionDecoder().load(cache)
        val phrases = QuickChatPhraseDecoder().load(cache)
        val enums = EnumDecoder()
        val items = ItemDecoder()
        val data = BufferReader((0..32).map { 0.toByte() }.toByteArray())
        for (i in 0..32784) {
            val def = options.getOrNull(i) ?: continue
            println(def)
        }
        for (i in phrases.indices) {
            // 612, 613, 614, 62
            val def = phrases.getOrNull(i) ?: continue
            println(def)
//            println(def.fillString(enums, items, data))
//            def.ids?.forEach {
//                for (id in it) {
//                    println(enums.get(id).map)
//                }
//            }
        }
    }
}