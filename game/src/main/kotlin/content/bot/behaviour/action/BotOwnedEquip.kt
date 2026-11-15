package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.BotItem
import content.bot.owned.OwnedResolvers
import content.entity.player.equip.Equipping
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.move
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * An owned bot wears what [items] asks for from its inventory, or takes off a slot the entry wants empty.
 * Direct equip, no interface clicks.
 */
data class BotOwnedEquip(val items: Map<EquipSlot, BotItem>) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        for ((slot, entry) in items) {
            val worn = player.equipped(slot)
            if (entry.ids.contains("empty")) {
                if (worn.isNotEmpty() && !player.equipment.move(slot.index, player.inventory)) {
                    return BehaviourState.Failed(Reason.Invalid("No room to take off ${worn.id.replace('_', ' ')}."))
                }
                continue
            }
            if (entry.ids.contains(worn.id)) {
                continue
            }
            val index = player.inventory.items.indexOfFirst { OwnedResolvers.valid(player, it, entry) }
            if (index == -1) {
                return BehaviourState.Failed(Reason.Invalid("Needs ${entry.ids.first().replace('_', ' ')} to wear."))
            }
            val item = player.inventory[index]
            if (!Equipping.equip(player, item, index)) {
                return BehaviourState.Failed(Reason.Invalid("Couldn't wear ${item.id.replace('_', ' ')}."))
            }
        }
        return BehaviourState.Success
    }
}
