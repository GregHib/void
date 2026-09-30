package content.bot.chat.api

import world.gregs.voidps.engine.Script

/**
 * Registers bot replies for player message intents.
 *
 * ```kotlin
 * class Greetings : Script, BotChatApi {
 *     init {
 *         botChat("greet") {
 *             say("Hello!", style = Style.Warm)
 *             say("Yo!", style = Style.Slang)
 *         }
 *     }
 * }
 * ```
 * Several handlers can respond to the same intent, each adds weighted candidates and one reply is picked.
 */
interface BotChatApi {

    fun botChat(vararg intents: String, handler: ChatContext.() -> Unit) {
        Script.checkLoading()
        for (intent in intents) {
            handlers.getOrPut(intent) { mutableListOf() }.add(handler)
        }
    }

    companion object : AutoCloseable {
        const val UNKNOWN = "unknown"
        const val ANNOYED = "annoyed"

        private val handlers = HashMap<String, MutableList<ChatContext.() -> Unit>>()

        fun handle(intent: String, context: ChatContext): Boolean {
            val list = handlers[intent] ?: return false
            for (handler in list) {
                handler(context)
            }
            return true
        }

        override fun close() {
            handlers.clear()
        }
    }
}