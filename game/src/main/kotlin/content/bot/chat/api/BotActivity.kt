package content.bot.chat.api

/**
 * What the bot is currently doing, taken from its behaviour's `produces` e.g. skill "mining", product "iron_ore"
 */
data class BotActivity(val skill: String, val product: String?)
