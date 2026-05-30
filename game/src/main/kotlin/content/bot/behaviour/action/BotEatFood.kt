package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.Condition
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractInterface

/**
 * Eats any food to keep hitpoints boosted.
 */
data class BotEatFood(
    val healPercentage: Int,
    val condition: Condition? = null,
) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        if (healPercentage <= 0) {
            return BehaviourState.Failed(Reason.Invalid("Invalid heal percentage $healPercentage"))
        }
        if (bot.levels.get(Skill.Constitution) > (bot.levels.getMax(Skill.Constitution) * healPercentage) / 100) {
            return BehaviourState.Success
        }
        if (player.contains("just_ate_food")) {
            return BehaviourState.Success
        }
        if (condition != null && !condition.check(player)) {
            return BehaviourState.Success
        }
        val inventory = player.inventory
        for (index in inventory.indices) {
            val item = inventory[index]
            if (item.isEmpty()) {
                continue
            }
            val option = item.def.options.indexOf("Eat")
            if (option == -1) {
                continue
            }
            val valid = world.execute(player, InteractInterface(149, 0, item.def.id, index, option))
            if (!valid) {
                return BehaviourState.Failed(Reason.Invalid("Invalid inventory interaction: ${item.def.id} $index $option"))
            }
            // Window for a follow-up brew reactive to chain on top of the food (overheal stack).
            player.start("just_ate_food", 2)
            return BehaviourState.Wait(1, BehaviourState.Running)
        }
        return BehaviourState.Success
    }
}
