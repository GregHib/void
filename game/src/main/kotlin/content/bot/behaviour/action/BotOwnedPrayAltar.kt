package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.condition.BotCarrying
import content.bot.behaviour.condition.BotSkillPercent
import content.bot.owned.owner
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.Spiral

/**
 * A step of an owned bot's recovery: prayer points back to full at the nearest altar (an area tagged `altar`,
 * walked to by the navigation graph). Skipped when prayer is already full, when the bot carries a prayer or
 * restore potion (its reactives drink those), and, with a word to the owner, when no altar can be reached.
 * One instance per recovery, as the trip keeps route state.
 */
class BotOwnedPrayAltar : BotAction {
    private val trip = BotGoToNearest(TAG)
    private var pray: BotInteractObject? = null
    private val full = BotSkillPercent(Skill.Prayer, minPercent = 100)

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        if (full.check(player) || POTIONS.any { it.check(player) }) {
            return BehaviourState.Success
        }
        val altars = Areas.tagged(TAG)
        if (altars.isEmpty()) {
            return BehaviourState.Success
        }
        if (altars.none { player.tile in it.area }) {
            val state = trip.update(bot, world, frame)
            return if (state is BehaviourState.Failed) skip(player, "it can't find a way to an altar") else BehaviourState.Running
        }
        if (player.mode != EmptyMode) {
            return BehaviourState.Running
        }
        if (pray == null) {
            val altar = nearby(player) ?: return skip(player, "there is no altar here")
            val option = altar.def(player).options?.firstOrNull { it != null && it.startsWith("Pray") } ?: return skip(player, "that altar can't be prayed at")
            pray = BotInteractObject(option, altar.id, success = full, radius = 0, x = altar.x, y = altar.y)
        }
        val state = pray!!.update(bot, world, frame)
        return if (state is BehaviourState.Failed) skip(player, "it couldn't use the altar") else BehaviourState.Running
    }

    private fun skip(player: Player, why: String): BehaviourState {
        player.owner?.message("[Companion ${player["owned_slot", 0] + 1}] ${player.name} couldn't recharge its prayer: $why.")
        return BehaviourState.Success
    }

    companion object {
        const val TAG = "altar"
        private val POTIONS = listOf(BotCarrying("prayer_potion_*"), BotCarrying("super_restore_*"))

        fun nearby(player: Player): GameObject? {
            for (tile in Spiral.spiral(player.tile, 12)) {
                for (obj in GameObjects.at(tile)) {
                    if (obj.id.startsWith("prayer_altar_")) {
                        return obj
                    }
                }
            }
            return null
        }
    }
}
