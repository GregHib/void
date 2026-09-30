package content.social.chat

import content.bot.chat.api.BotChatApi
import content.bot.chat.api.ChatContext
import content.bot.chat.api.ChatStyle
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
                sayQuick("Hello again.", weight = 2f, style = ChatStyle.Warm)
                sayQuick("Welcome back.", weight = 2f, style = ChatStyle.Warm)
            } else {
                sayQuick("Nice to meet you.", style = ChatStyle.Warm)
            }
            sayQuick("Hello!", style = ChatStyle.Warm)
            sayQuick("Hiya!", style = ChatStyle.Warm)
            sayQuick("Hi.")
            sayQuick("Hey.", style = ChatStyle.Blunt)
            sayQuick("Yo!", style = ChatStyle.Slang)
            sayQuick("Sup?", style = ChatStyle.Slang)
            say("hey", style = ChatStyle.Slang)
            say("Hi there!", style = ChatStyle.Warm)
        }

        botChat("greet") {
            val phrase = seasonal() ?: return@botChat
            sayQuick(phrase, weight = 4f)
        }

        botChat("farewell") {
            sayQuick("Bye!")
            sayQuick("See you later.")
            sayQuick("I'll see you around.", style = ChatStyle.Warm)
            sayQuick("Later!", style = ChatStyle.Slang)
            if (now.hour !in 4..<21) {
                sayQuick("Goodnight.", weight = 3f)
            }
        }

        botChat("thanks") {
            sayQuick("You're welcome.", style = ChatStyle.Formal)
            sayQuick("No problem.", style = ChatStyle.Formal)
            sayQuick("yw", style = ChatStyle.Slang)
            sayQuick("np", style = ChatStyle.Slang)
        }

        botChat("how_are_you") {
            sayQuick("I'm great!", style = ChatStyle.Warm)
            sayQuick("I'm good.")
            sayQuick("I'm okay.")
            sayQuick("Meh.", style = ChatStyle.Blunt)
            if (activity != null) {
                sayQuick("I am busy, sorry.", weight = 0.3f, style = ChatStyle.Blunt)
            }
        }

        botChat("feeling_good") {
            sayQuick("That's good.")
            sayQuick(":-)")
            sayQuick("Cool!", style = ChatStyle.Slang)
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
            sayQuick("lol", style = ChatStyle.Slang)
            sayQuick("Haha!", style = ChatStyle.Formal)
            sayQuick(":-D")
            sayQuick("Heh.", style = ChatStyle.Blunt)
            silence(1f - persona.chattiness)
        }

        botChat("compliment") {
            sayQuick("Thank you.", style = ChatStyle.Formal)
            sayQuick("ty", style = ChatStyle.Slang)
            sayQuick(":-D")
            sayQuick("You're cool.", weight = 0.5f, style = ChatStyle.Warm)
        }

        botChat("congratulate") {
            sayQuick("Thank you.", style = ChatStyle.Formal)
            sayQuick("ty", style = ChatStyle.Slang)
            sayQuick("w00t!", style = ChatStyle.Slang)
        }

        botChat("level_up") {
            val skill = utterance.first(ChatEntityType.Skill)
            if (skill != null) {
                sayQuick("Nice level in: <MultipleChoice>.", skill.key, weight = 2f, style = ChatStyle.Warm)
            }
            sayQuick("Congratulations.")
            sayQuick("Nice levels.")
            sayQuick("Awesome!", style = ChatStyle.Slang)
        }

        botChat("are_you_bot") {
            sayQuick("I can only use Quick Chat.")
            sayQuick("I am using Quick Chat.")
            sayQuick("No.", style = ChatStyle.Blunt)
            sayQuick("lol", style = ChatStyle.Slang)
            say("no lol", style = ChatStyle.Slang)
            say("No, are you?")
        }

        botChat("affirm", "deny") {
            sayQuick("Okay.")
            sayQuick(":-)")
            silence(2f)
        }

        botChat("insult") {
            sayQuick(":-(")
            sayQuick("That's bad.", style = ChatStyle.Blunt)
            sayQuick("Meh.", style = ChatStyle.Blunt)
            silence()
        }

        botChat(BotChatApi.ANNOYED) {
            if (conversation.annoyance > persona.patience) {
                sayQuick("I have added you to my ignore list.", then = { bot.ignores.add(speaker.accountName) })
                return@botChat
            }
            sayQuick("Please stop that.")
            sayQuick("Could you leave me alone, please?", style = ChatStyle.Formal)
        }

        // Replied when the player was talking to someone else
        botChat("not_you") {
            sayQuick("Sorry.", style = ChatStyle.Formal)
            sayQuick(":-O")
            sayQuick("Okay.", style = ChatStyle.Blunt)
            say("oh my bad", style = ChatStyle.Slang)
            say("oops lol", style = ChatStyle.Slang)
            say("Oh, sorry! I thought you were talking to me.", style = ChatStyle.Warm)
            silence(1f - persona.chattiness)
        }

        botChat("other") {
            silence(3f)
            sayQuick(":-S")
            sayQuick("O_o", style = ChatStyle.Slang)
        }

        // Not confident what was said, quick chat itself is the perfect excuse
        botChat(BotChatApi.UNKNOWN) {
            silence(2f)
            sayQuick("I can't answer that on Quick Chat.")
            sayQuick("Please don't ask difficult questions.", style = ChatStyle.Blunt)
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
