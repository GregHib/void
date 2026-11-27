package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.entity.combat.inCombat
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractInterface

/**
 * An owned bot eats what it carries until its hitpoints are full, or until it runs out of food: a step of its
 * recovery, or with [topUp] a reactive that keeps it full between fights (never during one, where the eat
 * threshold decides). Each mouthful is the food that heals the most without going to waste, else the smallest.
 */
data class BotOwnedHeal(val topUp: Boolean = false) : BotAction {

    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        val missing = player.levels.getMax(Skill.Constitution) - player.levels.get(Skill.Constitution)
        if (missing <= 0 || (topUp && player.inCombat)) {
            return BehaviourState.Success
        }
        if (player.hasClock("just_ate_food")) {
            return BehaviourState.Running
        }
        val index = pickFood(player, missing) ?: return BehaviourState.Success
        val item = player.inventory[index]
        val option = item.def.options.indexOf("Eat")
        if (!world.execute(player, InteractInterface(149, 0, item.def.id, index, option))) {
            return BehaviourState.Failed(Reason.Invalid("Invalid inventory interaction: ${item.def.id} $index $option"))
        }
        player.start("just_ate_food", 2)
        return BehaviourState.Running
    }

    companion object {
        fun hasFood(player: Player): Boolean = foodIndex(player) != -1

        /** The first carried item that can be eaten, or -1. */
        fun foodIndex(player: Player): Int {
            val inventory = player.inventory
            for (index in inventory.indices) {
                val item = inventory[index]
                if (item.isNotEmpty() && item.def.options.contains("Eat")) {
                    return index
                }
            }
            return -1
        }

        /** The carried food that heals the most of [missing] without waste, else the one that wastes least; null for none. */
        fun pickFood(player: Player, missing: Int): Int? {
            val inventory = player.inventory
            var best: Int? = null
            var bestHeal = 0
            var smallest: Int? = null
            var smallestHeal = Int.MAX_VALUE
            for (index in inventory.indices) {
                val item = inventory[index]
                if (item.isEmpty() || !item.def.options.contains("Eat")) {
                    continue
                }
                val heal = BotOwnedRecover.heals(item.id)
                if (heal <= missing && heal > bestHeal) {
                    best = index
                    bestHeal = heal
                }
                if (heal < smallestHeal) {
                    smallest = index
                    smallestHeal = heal
                }
            }
            return best ?: smallest
        }
    }
}
