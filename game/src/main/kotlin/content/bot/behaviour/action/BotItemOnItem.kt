package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.Condition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractInterfaceItem

data class BotItemOnItem(val item: String, val on: String, val success: Condition? = null) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState? {
        if (success != null && success.check(bot.player)) {
            return BehaviourState.Success
        }
        val state = itemOnItem(bot.player, world, item, on)
        if (state != null) {
            return state
        }
        return when {
            success == null -> BehaviourState.Wait(1, BehaviourState.Success)
            success.check(bot.player) -> BehaviourState.Success
            else -> BehaviourState.Running
        }
    }

    companion object {
        fun itemOnItem(player: Player, world: BotWorld, item: String, on: String): BehaviourState? {
            val inventory = player.inventory
            val fromSlot = inventory.indexOf(item)
            if (fromSlot == -1) {
                return BehaviourState.Failed(Reason.Invalid("No inventory item '$item'."))
            }
            val toSlot = inventory.indexOf(on)
            if (toSlot == -1) {
                return BehaviourState.Failed(Reason.Invalid("No inventory item '$on'."))
            }
            val from = inventory[fromSlot]
            val to = inventory[toSlot]
            val valid = world.execute(player, InteractInterfaceItem(from.def.id, to.def.id, fromSlot, toSlot, 149, 0, 149, 0))
            if (valid) {
                return null
            }
            return BehaviourState.Failed(Reason.Invalid("Invalid item on item: ${from.def.id}:$fromSlot -> ${to.def.id}:$toSlot."))
        }
    }
}
