package content.social.chat

import content.bot.behaviour.Reason
import content.bot.bot
import content.bot.chat.api.BotChatApi
import content.bot.chat.api.ChatContext
import content.bot.chat.api.ChatLocations
import content.bot.chat.api.Style
import content.bot.isBot
import world.gregs.voidps.engine.Script

/**
 * What a bot is up to: where it's going and whether it's stuck
 */
class StatusChat :
    Script,
    BotChatApi {

    init {
        botChat("ask_destination") {
            if (stuck) {
                stuck()
                return@botChat
            }
            val destination = destination
            if (destination == null) {
                type("Nowhere, I'm staying here.", style = Style.Formal)
                type("nowhere", style = Style.Slang)
                say("Not right now.")
                val activity = activity ?: return@botChat
                say("I am training: <MultipleChoice>.", activity.skill, weight = 0.5f)
                return@botChat
            }
            val name = destination.location?.let { ChatLocations.name(it) }
            if (destination.bank) {
                say("I have to go to a bank.", weight = 3f)
                type("bank", style = Style.Slang)
                if (name != null) {
                    type("To the bank in $name.", weight = 2f, style = Style.Formal)
                } else {
                    type("Just going to the bank.", style = Style.Formal)
                }
                return@botChat
            }
            if (name != null) {
                say("Follow me to: <MultipleChoice>.", destination.location!!, style = Style.Warm)
                type("Heading to $name.", weight = 2f, style = Style.Formal)
                type("$name lol", style = Style.Slang)
            }
        }

        botChat("ask_stuck") {
            if (stuck) {
                stuck()
                return@botChat
            }
            say("No.", style = Style.Curt)
            say("I'm okay.")
            type("nope", style = Style.Slang)
            type("No, I'm fine thanks.", style = Style.Warm)
            if (destination?.bank == true) {
                say("I have to go to a bank.")
            }
            val activity = activity
            if (activity != null) {
                say("I am training: <MultipleChoice>.", activity.skill, weight = 0.5f)
            }
        }

        botChat("ask_activity") {
            if (stuck) {
                stuck()
                return@botChat
            }
            val destination = destination ?: return@botChat
            if (destination.bank) {
                say("I have to go to a bank.", weight = 2f)
                type("banking", style = Style.Slang)
                return@botChat
            }
            val location = destination.location ?: return@botChat
            type("Heading to ${ChatLocations.name(location)}.")
        }
    }

    /**
     * Admit to being stuck and have another go at getting there, a soft fail drops the current route so it's recalculated
     */
    private fun ChatContext.stuck() {
        val unstick = {
            if (bot.isBot && !bot.bot.noTask()) {
                bot.bot.frames.peek().fail(Reason.Stuck)
            }
        }
        say("Yes.", then = unstick)
        say("Help!", then = unstick)
        type("yeah :(", style = Style.Slang, then = unstick)
        type("Yes, I can't seem to get past here.", weight = 2f, style = Style.Formal, then = unstick)
    }
}
