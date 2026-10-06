package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import world.gregs.voidps.engine.inv.inventory

/** Drops every matching item through the inventory interface, optionally retaining stacks. */
data class BotDropItems(val id: String, val keepStackable: Boolean = false) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val inventory = bot.player.inventory
        if (!inventory.contains(id) || keepStackable && inventory.stackable(id)) {
            return BehaviourState.Success
        }
        return when (val state = BotInterfaceOption("Drop", "inventory:inventory:$id").update(bot, world, frame)) {
            is BehaviourState.Wait -> BehaviourState.Wait(state.ticks, BehaviourState.Running)
            else -> state ?: BehaviourState.Running
        }
    }
}
