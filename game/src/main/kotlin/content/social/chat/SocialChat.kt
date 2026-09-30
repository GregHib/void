package content.social.chat

import content.bot.chat.api.BotChatApi
import content.bot.chat.api.ChatContext
import content.bot.chat.api.Style
import content.bot.chat.tag.ChatEntityType
import world.gregs.voidps.engine.Script
import java.time.Month

class SocialChat :
    Script,
    BotChatApi {

    init {
        botChat("greet") {
            if (conversation.said("greet")) {
                say(":-)")
                silence()
                return@botChat
            }
            if (conversation.sessions > 1) {
                say("Hello again.", weight = 2f, style = Style.Warm)
                say("Welcome back.", weight = 2f, style = Style.Warm)
            } else {
                say("Nice to meet you.", style = Style.Warm)
            }
            say("Hello!", style = Style.Warm)
            say("Hiya!", style = Style.Warm)
            say("Hi.")
            say("Hey.", style = Style.Curt)
            say("Yo!", style = Style.Slang)
            say("Sup?", style = Style.Slang)
            type("hey", style = Style.Slang)
            type("Hi there!", style = Style.Warm)
        }

        botChat("greet") {
            val phrase = seasonal() ?: return@botChat
            say(phrase, weight = 4f)
        }

        botChat("farewell") {
            say("Bye!")
            say("See you later.")
            say("I'll see you around.", style = Style.Warm)
            say("Later!", style = Style.Slang)
            if (now.hour !in 4..<21) {
                say("Goodnight.", weight = 3f)
            }
        }

        botChat("thanks") {
            say("You're welcome.", style = Style.Formal)
            say("No problem.", style = Style.Formal)
            say("yw", style = Style.Slang)
            say("np", style = Style.Slang)
        }

        botChat("how_are_you") {
            say("I'm great!", style = Style.Warm)
            say("I'm good.")
            say("I'm okay.")
            say("Meh.", style = Style.Curt)
            if (activity != null) {
                say("I am busy, sorry.", weight = 0.3f, style = Style.Curt)
            }
        }

        botChat("feeling_good") {
            say("That's good.")
            say(":-)")
            say("Cool!", style = Style.Slang)
        }

        botChat("sad") {
            say("Oh no!")
            say(":-(")
            say("That's bad.")
            if (utterance.mentions("died") || utterance.mentions("killed") || utterance.mentions("failed")) {
                say("Unlucky, maybe next time.", weight = 2f)
            }
        }

        botChat("laugh") {
            say("lol", style = Style.Slang)
            say("Haha!", style = Style.Formal)
            say(":-D")
            say("Heh.", style = Style.Curt)
            silence(1f - persona.chattiness)
        }

        botChat("compliment") {
            say("Thank you.", style = Style.Formal)
            say("ty", style = Style.Slang)
            say(":-D")
            say("You're cool.", weight = 0.5f, style = Style.Warm)
        }

        botChat("congratulate") {
            say("Thank you.", style = Style.Formal)
            say("ty", style = Style.Slang)
            say("w00t!", style = Style.Slang)
        }

        botChat("level_up") {
            val skill = utterance.first(ChatEntityType.Skill)
            if (skill != null) {
                say("Nice level in: <MultipleChoice>.", skill.key, weight = 2f, style = Style.Warm)
            }
            say("Congratulations.")
            say("Nice levels.")
            say("Awesome!", style = Style.Slang)
        }

        botChat("are_you_bot") {
            say("I can only use Quick Chat.")
            say("I am using Quick Chat.")
            say("No.", style = Style.Curt)
            say("lol", style = Style.Slang)
            type("no lol", style = Style.Slang)
            type("No, are you?")
        }

        botChat("affirm", "deny") {
            say("Okay.")
            say(":-)")
            silence(2f)
        }

        botChat("insult") {
            say(":-(")
            say("That's bad.", style = Style.Curt)
            say("Meh.", style = Style.Curt)
            silence()
        }

        botChat(BotChatApi.ANNOYED) {
            if (conversation.annoyance > persona.patience) {
                say("I have added you to my ignore list.", then = { bot.ignores.add(speaker.accountName) })
                return@botChat
            }
            say("Please stop that.")
            say("Could you leave me alone, please?", style = Style.Formal)
        }

        // Replied when the player was talking to someone else
        botChat("not_you") {
            say("Sorry.", style = Style.Formal)
            say(":-O")
            say("Okay.", style = Style.Curt)
            type("oh my bad", style = Style.Slang)
            type("oops lol", style = Style.Slang)
            type("Oh, sorry! I thought you were talking to me.", style = Style.Warm)
            silence(1f - persona.chattiness)
        }

        botChat("other") {
            silence(3f)
            say(":-S")
            say("O_o", style = Style.Slang)
        }

        // Not confident what was said, quick chat itself is the perfect excuse
        botChat(BotChatApi.UNKNOWN) {
            silence(2f)
            say("I can't answer that on Quick Chat.")
            say("Please don't ask difficult questions.", style = Style.Curt)
            say("I don't know.")
            say(":-S")
        }
    }

    private fun ChatContext.seasonal(): String? = when (now.month) {
        Month.DECEMBER if now.dayOfMonth in 20..26 -> "Merry Christmas!"
        Month.JANUARY if now.dayOfMonth == 1 -> "Happy New Year!"
        Month.OCTOBER if now.dayOfMonth == 31 -> "Happy Hallowe'en!"
        else -> null
    }
}
