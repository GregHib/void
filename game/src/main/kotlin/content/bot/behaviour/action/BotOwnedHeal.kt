package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory

/**
 * A step of an owned bot's recovery: eats what it carries until its hitpoints are full, or until it runs out
 * of food. Unlike the reactive [BotEatFood] it keeps going from one mouthful to the next.
 */
class BotOwnedHeal : BotAction {
    private val eat = BotEatFood(100)

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        if (player.levels.get(Skill.Constitution) >= player.levels.getMax(Skill.Constitution)) {
            return BehaviourState.Success
        }
        if (player.hasClock("just_ate_food")) {
            return BehaviourState.Running
        }
        if (!hasFood(player)) {
            return BehaviourState.Success
        }
        val state = eat.update(bot, world, frame)
        return if (state is BehaviourState.Failed) state else BehaviourState.Running
    }

    companion object {
        fun hasFood(player: world.gregs.voidps.engine.entity.character.player.Player): Boolean = foodIndex(player) != -1

        /** The first carried item that can be eaten, or -1. */
        fun foodIndex(player: world.gregs.voidps.engine.entity.character.player.Player): Int {
            val inventory = player.inventory
            for (index in inventory.indices) {
                val item = inventory[index]
                if (item.isNotEmpty() && item.def.options.contains("Eat")) {
                    return index
                }
            }
            return -1
        }
    }
}
