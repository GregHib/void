package content.quest

import content.bot.chat.api.BotChatApi
import world.gregs.voidps.engine.Script

class QuestChat :
    Script,
    BotChatApi {
    init {
        botChat("ask_quest_points") {
            say("I have <Varp> Quest Points.")
        }
    }
}
