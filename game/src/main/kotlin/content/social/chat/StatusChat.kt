package content.social.chat

import content.bot.behaviour.Reason
import content.bot.behaviour.skill
import content.bot.bot
import content.bot.chat.api.BotChatApi
import content.bot.chat.api.ChatContext
import content.bot.chat.api.ChatLocations
import content.bot.chat.api.ChatStyle
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
                say("Nowhere, I'm staying here.", style = ChatStyle.Formal)
                say("nowhere", style = ChatStyle.Slang)
                sayQuick("Not right now.")
                val skill = activity?.skill ?: return@botChat
                sayQuick("I am training: <MultipleChoice>.", skill, weight = 0.5f)
                return@botChat
            }
            val location = ChatLocations.nearest(destination)
            val name = location?.let { ChatLocations.name(it) }
            if ("bank" in destination.tags) {
                sayQuick("I have to go to a bank.", weight = 3f)
                say("bank", style = ChatStyle.Slang)
                if (name != null) {
                    say("To the bank in $name.", weight = 2f, style = ChatStyle.Formal)
                } else {
                    say("Just going to the bank.", style = ChatStyle.Formal)
                }
                return@botChat
            }
            if (location != null) {
                sayQuick("Follow me to: <MultipleChoice>.", location, style = ChatStyle.Warm)
                say("Heading to $name.", weight = 2f, style = ChatStyle.Formal)
                say("$name lol", style = ChatStyle.Slang)
            }
        }

        botChat("ask_stuck") {
            if (stuck) {
                stuck()
                return@botChat
            }
            sayQuick("No.", style = ChatStyle.Blunt)
            sayQuick("I'm okay.")
            say("nope", style = ChatStyle.Slang)
            say("No, I'm fine thanks.", style = ChatStyle.Warm)
            if (destination?.tags?.contains("bank") == true) {
                sayQuick("I have to go to a bank.")
            }
            val skill = activity?.skill
            if (skill != null) {
                sayQuick("I am training: <MultipleChoice>.", skill, weight = 0.5f)
            }
        }

        botChat("ask_activity") {
            if (stuck) {
                stuck()
                return@botChat
            }
            val destination = destination ?: return@botChat
            if ("bank" in destination.tags) {
                sayQuick("I have to go to a bank.", weight = 2f)
                say("banking", style = ChatStyle.Slang)
                return@botChat
            }
            val location = ChatLocations.nearest(destination) ?: return@botChat
            say("Heading to ${ChatLocations.name(location)}.")
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
        sayQuick("Yes.", then = unstick)
        sayQuick("Help!", then = unstick)
        say("yeah :(", style = ChatStyle.Slang, then = unstick)
        say("Yes, I can't seem to get past here.", weight = 2f, style = ChatStyle.Formal, then = unstick)
    }
}
