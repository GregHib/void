package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax

/**
 * Reactive: a melee companion with the level and the runes keeps Vengeance up, casting it again whenever it has
 * been spent or has worn off; [BotCastVengeance] opens the lunar book for it. Only the level and the runes are
 * asked of it, never the owner's own book.
 */
object BotOwnedVengeance : BotAction {
    private const val MIN_MAGIC = 94

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (!bot.player.hasMax(Skill.Magic, MIN_MAGIC)) {
            return BehaviourState.Success
        }
        return BotCastVengeance.update(bot, world, frame)
    }
}
