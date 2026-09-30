package content.bot.chat.api

import content.bot.Bot
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.action.BotGoTo
import content.bot.behaviour.action.BotGoToNearest
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.data.definition.Areas

/**
 * The [area] a bot is walking to and the quick chat [location] nearest to it
 */
data class BotDestination(val area: AreaDefinition, val location: String?) {
    val bank: Boolean
        get() = "bank" in area.tags

    companion object {
        /**
         * Top-most running go to action, resolvers sit above their activity so a trip to the bank is found before the activity's own area
         */
        fun of(bot: Bot): BotDestination? {
            val frames = bot.frames
            for (index in frames.indices.reversed()) {
                val frame = frames[index]
                if (frame.state == BehaviourState.Pending || frame.completed()) {
                    continue
                }
                val area = when (val action = frame.action()) {
                    is BotGoTo -> Areas.getOrNull(action.target)
                    is BotGoToNearest -> Areas.tagged(action.tag).minByOrNull { it.area.first().distanceTo(bot.tile) }
                    else -> null
                } ?: continue
                if (bot.tile in area.area) {
                    return null
                }
                return BotDestination(area, ChatLocations.nearest(area.area.first())?.first)
            }
            return null
        }
    }
}
