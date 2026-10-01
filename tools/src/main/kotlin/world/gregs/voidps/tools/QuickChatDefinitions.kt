package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.QuickChatOptionDefinition
import world.gregs.voidps.cache.definition.data.QuickChatPhraseDefinition
import world.gregs.voidps.cache.definition.decoder.QuickChatOptionDecoder
import world.gregs.voidps.cache.definition.decoder.QuickChatPhraseDecoder
import world.gregs.voidps.engine.data.Settings

object QuickChatDefinitions {
    private var set = mutableSetOf<String?>()

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val options = QuickChatOptionDecoder().load(cache)
        val phrases = QuickChatPhraseDecoder().load(cache)
        val children = options.flatMap { it.quickReplyOptions?.toList() ?: emptyList() }.toSet()
        for (option in options) {
            if (option.optionText == null || option.id in children) {
                continue
            }
            println("${option.optionText}:")
            printChildren(options, phrases, option, 0)
            println()
        }
        println("Option count: ${set.size}")
    }

    private fun printChildren(options: Array<QuickChatOptionDefinition>, phrases: Array<QuickChatPhraseDefinition>, option: QuickChatOptionDefinition, depth: Int) {
        val indent = "    ".repeat(depth)
        val subMenus = option.quickReplyOptions
        if (subMenus != null) {
            for (i in subMenus.indices) {
                val child = options.getOrNull(subMenus[i]) ?: continue
                println("$indent${option.navigateChars!![i].uppercaseChar()}. ${child.optionText}")
                printChildren(options, phrases, child, depth + 1)
            }
        }
        val phraseIds = option.dynamicData
        if (phraseIds != null) {
            for (i in phraseIds.indices) {
                val phrase = phrases.getOrNull(phraseIds[i]) ?: continue
                val text = text(phrase)
                set.add(text)
                println("$indent${(i + 1) % 10}. $text")
            }
        }
    }

    private fun text(phrase: QuickChatPhraseDefinition): String {
        val parts = phrase.stringParts ?: return ""
        return buildString {
            for (index in parts.indices) {
                append(parts[index])
                if (index < parts.lastIndex) {
                    append('<').append(phrase.getType(index)?.name ?: "?").append('>')
                }
            }
        }
    }
}
