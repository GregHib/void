package world.gregs.voidps.tools

import org.koin.core.context.startKoin
import org.koin.fileProperties
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.QuickChatOptionDecoder
import world.gregs.voidps.cache.definition.decoder.QuickChatPhraseDecoder
import world.gregs.voidps.engine.client.cacheDefinitionModule
import world.gregs.voidps.engine.client.cacheModule

object QuickChatDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        val koin = startKoin {
            fileProperties("/tool.properties")
            modules(cacheModule, cacheDefinitionModule)
        }.koin
        val options = QuickChatOptionDecoder(koin.get())
        val phrases = QuickChatPhraseDecoder(koin.get())
        val enums = EnumDecoder(koin.get())
        val items = ItemDecoder(koin.get())
        val reader = BufferReader((0..32).map { 0.toByte() }.toByteArray())
        for (i in options.indices) {
            val def = options.getOrNull(i) ?: continue
            println(def)
        }

        for (i in phrases.indices) {
            val def = phrases.getOrNull(i) ?: continue
            println(def)
            println(def.method3216(enums, items, phrases, reader))
        }
    }
}