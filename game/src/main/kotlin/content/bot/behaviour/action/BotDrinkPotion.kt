package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.Condition
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractInterface

/**
 * Drinks a potion dose to keep [skill] boosted. Re-doses whenever the boost has fully decayed
 * (`current <= max`). No-ops once the inventory runs out, so the activity loop continues.
 */
data class BotDrinkPotion(
    val item: String,
    val skill: Skill,
    val condition: Condition? = null,
) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        if (player.hasClock("drink_delay")) return BehaviourState.Success
        if (player.levels.get(skill) > player.levels.getMax(skill)) return BehaviourState.Success
        if (condition != null && !condition.check(player)) return BehaviourState.Success
        val inv = player.inventory
        for (index in inv.indices) {
            val slotItem = inv[index]
            if (slotItem.isEmpty()) continue
            if (!wildcardEquals(item, slotItem.id)) continue
            val option = slotItem.def.options.indexOf("Drink")
            if (option == -1) continue
            val valid = world.execute(player, InteractInterface(149, 0, slotItem.def.id, index, option))
            if (!valid) {
                return BehaviourState.Failed(Reason.Invalid("Invalid potion drink: ${slotItem.id} $index $option"))
            }
            return BehaviourState.Wait(1, BehaviourState.Running)
        }
        return BehaviourState.Success
    }
}
