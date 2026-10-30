package world.gregs.voidps.tools.model

import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.QuickChatType
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.cache.definition.decoder.QuickChatPhraseDecoder
import world.gregs.voidps.engine.data.Settings

object QuickChatEnumDump {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache = CacheDelegate(Settings["storage.cache.path"])
        val phrases = QuickChatPhraseDecoder().load(cache)
        val enums = EnumDecoder().load(cache)
        for (phrase in phrases) {
            val parts = phrase.stringParts ?: continue
            val text = parts.indices.joinToString("") { i -> parts[i] + if (i < parts.lastIndex) "<${phrase.getType(i)?.name}>" else "" }
            println("${phrase.id}: $text")
            val types = phrase.types ?: continue
            for (i in types.indices) {
                if (phrase.getType(i) == QuickChatType.MultipleChoice) {
                    val id = phrase.ids!![i].first()
                    println("    enum $id: ${enums[id].map?.values}")
                }
            }
        }
    }
}
