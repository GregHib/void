package world.gregs.voidps.tools.model

import content.bot.chat.BotChatModel
import kotlin.system.measureNanoTime

/**
 * Type messages and see how bots understand them. Retrains the cached model first if the examples have changed.
 */
object BotChatConsole {
    @JvmStatic
    fun main(args: Array<String>) {
        val data = BotChatData()
        val processor = BotChatModel().load(data.files, data.quests).chatProcessor ?: return
        println("Type a message (blank line to exit):")
        while (true) {
            val line = readlnOrNull()?.takeIf { it.isNotBlank() } ?: break
            val nanos = measureNanoTime {
                val utterance = processor.parse(line)
                println("  ${utterance.intent} %.2f ${utterance.tokens} ${utterance.entities.joinToString { "${it.type}=${it.key}" }}".format(utterance.confidence))
            }
            println("  %.3fms".format(nanos / 1_000_000.0))
        }
    }
}
