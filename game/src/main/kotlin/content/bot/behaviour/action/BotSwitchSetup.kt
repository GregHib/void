package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.condition.Condition
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.slot
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

data class BotSwitchSetup(val equipment: Map<EquipSlot, BotItem>, val condition: Condition? = null) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        if (condition != null && !condition.check(player)) {
            return BehaviourState.Success
        }
        for ((slot, entry) in equipment) {
            val worn = player.equipped(slot)
            if (entry.ids.contains(worn.id)) {
                continue
            }
            val inv = player.inventory
            var invSlot = -1
            var invItemId = -1
            for (index in inv.indices) {
                val item = inv[index]
                if (item.slot != slot) {
                    continue
                }
                if (!entry.ids.contains(item.id)) {
                    continue
                }
                invSlot = index
                invItemId = item.def.id
                break
            }
            if (invSlot == -1) {
                continue
            }
            val componentDef = InterfaceDefinitions.getComponent("inventory", "inventory")
                ?: return BehaviourState.Failed(Reason.Invalid("Missing interface component inventory:inventory."))
            val componentId = InterfaceDefinitions.getComponentId("inventory", "inventory")
                ?: return BehaviourState.Failed(Reason.Invalid("Missing interface component id inventory:inventory."))
            val options = componentDef.options ?: componentDef.getOrNull("options") ?: emptyArray()
            var optionIndex = options.indexOf("Equip")
            if (optionIndex == -1) {
                optionIndex = options.indexOf("Wield")
            }
            if (optionIndex == -1) {
                val def = InterfaceHandler.getInventory(player, "inventory", "inventory", componentDef)
                val candidate = if (def != null) player.inventories.inventory(def)[invSlot].def.options.indexOf("Wield") else -1
                if (candidate == -1) {
                    return BehaviourState.Failed(Reason.Invalid("No Equip/Wield option on inventory:inventory."))
                }
                optionIndex = candidate
            }
            val interfaceDef = InterfaceDefinitions.getOrNull("inventory")
                ?: return BehaviourState.Failed(Reason.Invalid("Missing interface id inventory."))
            val valid = world.execute(
                player,
                InteractInterface(
                    interfaceId = interfaceDef.id,
                    componentId = componentId,
                    itemId = invItemId,
                    itemSlot = invSlot,
                    option = optionIndex,
                ),
            )
            if (!valid) {
                return BehaviourState.Failed(Reason.Invalid("Invalid equip dispatch for slot $slot item $invItemId."))
            }
            return BehaviourState.Wait(1, BehaviourState.Running)
        }
        return BehaviourState.Success
    }
}
