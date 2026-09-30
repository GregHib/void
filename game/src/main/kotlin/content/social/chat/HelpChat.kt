package content.social.chat

import content.bot.chat.api.BotChatApi
import content.bot.chat.api.ChatContext
import content.bot.chat.api.ChatLocations
import content.bot.chat.api.Style
import content.bot.chat.tag.ChatEntityType
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.type.Tile
import kotlin.math.abs

class HelpChat :
    Script,
    BotChatApi {

    init {
        botChat("ask_help") {
            if (!chance(persona.helpfulness)) {
                say("I am busy, sorry.")
                say("Can you ask someone else? Sorry.")
                return@botChat
            }
            say("Of course.")
            say("Okay.")
            say("What are you doing?", weight = 0.5f)
        }

        botChat("lost") {
            val target = utterance.first(ChatEntityType.Location)
            if (target == null) {
                // Ask first and wait for yes/no
                asking("offer_help", yes = { say("Try looking in the Game Guide.") }, no = { say("Okay.") }) {
                    say("Do you need help?", weight = 3f)
                }
                say("Try looking in the Game Guide.", style = Style.Curt)
                return@botChat
            }
            val tile = Tables.tileOrNull("locations.${target.key}.tile")
            val (landmark, direction) = directions(tile) ?: (null to null)
            if (landmark != null && direction != null) {
                say("That is <MultipleChoice> of <MultipleChoice>.", direction, landmark, weight = 2f)
            }
            say("Go to location: <MultipleChoice>.", target.key, weight = 0.5f)
        }

        botChat("ask_where_item") {
            val item = slot(ChatEntityType.Item)
            val location = item?.let { Tables.stringOrNull("chat_item_sources.${it.key}.location") }
            if (location.isNullOrEmpty()) {
                say("I don't know.")
                say("Try looking in the Game Guide.")
                return@botChat
            }
            say("Go to location: <MultipleChoice>.", location, weight = 3f)
        }

        // Bots can't act on these yet so politely decline
        botChat("follow_request", "ask_for_item", "trade_item") {
            say("Not right now.")
            say("I am busy, sorry.")
            say("No thank you.", style = Style.Formal)
            say("Sorry.")
        }
    }

    /**
     * Direction of [target] from the known location nearest to the bot, e.g. Varrock is "north" of "lumbridge"
     */
    private fun ChatContext.directions(target: Tile?): Pair<String, String>? {
        if (target == null || target.x == 0) {
            return null
        }
        val landmark = ChatLocations.nearest(bot.tile) ?: return null
        val direction = compass(target.x - landmark.second.x, target.y - landmark.second.y) ?: return null
        return landmark.first to direction
    }

    private fun compass(dx: Int, dy: Int): String? {
        if (abs(dx) < 20 && abs(dy) < 20) {
            return null
        }
        val vertical = when {
            dy > abs(dx) / 2 -> "north"
            dy < -abs(dx) / 2 -> "south"
            else -> ""
        }
        val horizontal = when {
            dx > abs(dy) / 2 -> "east"
            dx < -abs(dy) / 2 -> "west"
            else -> ""
        }
        return if (vertical.isNotEmpty() && horizontal.isNotEmpty()) "$vertical-$horizontal" else vertical + horizontal
    }
}
