package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.condition.Condition
import content.bot.behaviour.Reason
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.network.client.instruction.InteractInterfaceObject
import kotlin.collections.iterator

data class BotItemOnObject(val item: String, val id: String, val success: Condition? = null) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        if (success != null && success.check(bot.player)) {
            return BehaviourState.Success
        }
        val inventory = bot.player.inventory
        val slot = inventory.indexOf(this@BotItemOnObject.item)
        if (slot == -1) {
            return BehaviourState.Failed(Reason.Invalid("No inventory item '${this@BotItemOnObject.item}'."))
        }
        val item = inventory[slot]
        return search(bot, world, item, slot)
    }

    private fun search(bot: Bot, world: BotWorld, item: Item, slot: Int): BehaviourState {
        val player = bot.player
        val ids = if (id.contains(",")) id.split(",") else listOf(id)
        for (tile in Spiral.spiral(player.tile, 10)) {
            for (obj in GameObjects.at(tile)) {
                if (ids.none { wildcardEquals(it, obj.id) }) {
                    continue
                }
                val valid = world.execute(bot.player, InteractInterfaceObject(obj.intId, obj.x, obj.y, 149, 0, item.def.id, slot))
                if (!valid) {
                    return BehaviourState.Failed(Reason.Invalid("Invalid item on object: ${item.def.id}:$slot -> $obj."))
                }
                return BehaviourState.Running
            }
        }
        if (success == null) {
            return BehaviourState.Failed(Reason.NoTarget)
        }
        if (success.check(bot.player)) {
            return BehaviourState.Success
        }
        return BehaviourState.Running
    }
}
