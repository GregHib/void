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
        // Prayer caps at max (super_restore can't overshoot), so drinking at current == max
        // wastes a dose. Boost potions overshoot, so for them the original `current > max`
        // check is correct (drink while at-or-below-max means boost has decayed).
        val current = player.levels.get(skill)
        val maxLevel = player.levels.getMax(skill)
        val full = if (skill == Skill.Prayer) current >= maxLevel else current > maxLevel
        if (full) return BehaviourState.Success
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
            // Track Saradomin-brew dose count vs the last restore so templates can fire a
            // super_restore once brews have stacked enough combat-stat debuff to matter.
            // Restore potions reset the counter (they undo the debuff).
            val drunkId = slotItem.id
            if (drunkId.startsWith("saradomin_brew")) {
                player["brew_doses_since_restore"] = (player.get<Int>("brew_doses_since_restore") ?: 0) + 1
            } else if (drunkId.startsWith("super_restore") || drunkId.startsWith("restore_potion")) {
                player["brew_doses_since_restore"] = 0
            }
            return BehaviourState.Wait(1, BehaviourState.Running)
        }
        return BehaviourState.Success
    }
}
