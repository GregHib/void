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
                sayQuick(":-)")
                silence()
                return@botChat
            }
            if (conversation.sessions > 1) {
                sayQuick("Hello again.", weight = 2f, style = Style.Warm)
                sayQuick("Welcome back.", weight = 2f, style = Style.Warm)
            } else {
                sayQuick("Nice to meet you.", style = Style.Warm)
            }
            sayQuick("Hello!", style = Style.Warm)
            sayQuick("Hiya!", style = Style.Warm)
            sayQuick("Hi.")
            sayQuick("Hey.", style = Style.Curt)
            sayQuick("Yo!", style = Style.Slang)
            sayQuick("Sup?", style = Style.Slang)
            say("hey", style = Style.Slang)
            say("Hi there!", style = Style.Warm)
        }

        botChat("greet") {
            val phrase = seasonal() ?: return@botChat
            sayQuick(phrase, weight = 4f)
        }

        botChat("farewell") {
            sayQuick("Bye!")
            sayQuick("See you later.")
            sayQuick("I'll see you around.", style = Style.Warm)
            sayQuick("Later!", style = Style.Slang)
            if (now.hour !in 4..<21) {
                sayQuick("Goodnight.", weight = 3f)
            }
        }

        botChat("thanks") {
            sayQuick("You're welcome.", style = Style.Formal)
            sayQuick("No problem.", style = Style.Formal)
            sayQuick("yw", style = Style.Slang)
            sayQuick("np", style = Style.Slang)
        }

        botChat("how_are_you") {
            sayQuick("I'm great!", style = Style.Warm)
            sayQuick("I'm good.")
            sayQuick("I'm okay.")
            sayQuick("Meh.", style = Style.Curt)
            if (activity != null) {
                sayQuick("I am busy, sorry.", weight = 0.3f, style = Style.Curt)
            }
        }

        botChat("feeling_good") {
            sayQuick("That's good.")
            sayQuick(":-)")
            sayQuick("Cool!", style = Style.Slang)
        }

        botChat("sad") {
            sayQuick("Oh no!")
            sayQuick(":-(")
            sayQuick("That's bad.")
            if (utterance.mentions("died") || utterance.mentions("killed") || utterance.mentions("failed")) {
                sayQuick("Unlucky, maybe next time.", weight = 2f)
            }
        }

        botChat("laugh") {
            sayQuick("lol", style = Style.Slang)
            sayQuick("Haha!", style = Style.Formal)
            sayQuick(":-D")
            sayQuick("Heh.", style = Style.Curt)
            silence(1f - persona.chattiness)
        }

        botChat("compliment") {
            sayQuick("Thank you.", style = Style.Formal)
            sayQuick("ty", style = Style.Slang)
            sayQuick(":-D")
            sayQuick("You're cool.", weight = 0.5f, style = Style.Warm)
        }

        botChat("congratulate") {
            sayQuick("Thank you.", style = Style.Formal)
            sayQuick("ty", style = Style.Slang)
            sayQuick("w00t!", style = Style.Slang)
        }

        botChat("level_up") {
            val skill = utterance.first(ChatEntityType.Skill)
            if (skill != null) {
                sayQuick("Nice level in: <MultipleChoice>.", skill.key, weight = 2f, style = Style.Warm)
            }
            sayQuick("Congratulations.")
            sayQuick("Nice levels.")
            sayQuick("Awesome!", style = Style.Slang)
        }

        botChat("are_you_bot") {
            sayQuick("I can only use Quick Chat.")
            sayQuick("I am using Quick Chat.")
            sayQuick("No.", style = Style.Curt)
            sayQuick("lol", style = Style.Slang)
            say("no lol", style = Style.Slang)
            say("No, are you?")
        }

        botChat("affirm", "deny") {
            sayQuick("Okay.")
            sayQuick(":-)")
            silence(2f)
        }

        botChat("insult") {
            sayQuick(":-(")
            sayQuick("That's bad.", style = Style.Curt)
            sayQuick("Meh.", style = Style.Curt)
            silence()
        }

        botChat(BotChatApi.ANNOYED) {
            if (conversation.annoyance > persona.patience) {
                sayQuick("I have added you to my ignore list.", then = { bot.ignores.add(speaker.accountName) })
                return@botChat
            }
            sayQuick("Please stop that.")
            sayQuick("Could you leave me alone, please?", style = Style.Formal)
        }

        // Replied when the player was talking to someone else
        botChat("not_you") {
            sayQuick("Sorry.", style = Style.Formal)
            sayQuick(":-O")
            sayQuick("Okay.", style = Style.Curt)
            say("oh my bad", style = Style.Slang)
            say("oops lol", style = Style.Slang)
            say("Oh, sorry! I thought you were talking to me.", style = Style.Warm)
            silence(1f - persona.chattiness)
        }

        botChat("other") {
            silence(3f)
            sayQuick(":-S")
            sayQuick("O_o", style = Style.Slang)
        }

        // Not confident what was said, quick chat itself is the perfect excuse
        botChat(BotChatApi.UNKNOWN) {
            silence(2f)
            sayQuick("I can't answer that on Quick Chat.")
            sayQuick("Please don't ask difficult questions.", style = Style.Curt)
            sayQuick("I don't know.")
            sayQuick(":-S")
        }
    }

    private fun ChatContext.seasonal(): String? = when (now.month) {
        Month.DECEMBER if now.dayOfMonth in 20..26 -> "Merry Christmas!"
        Month.JANUARY if now.dayOfMonth == 1 -> "Happy New Year!"
        Month.OCTOBER if now.dayOfMonth == 31 -> "Happy Hallowe'en!"
        else -> null
    }
}
