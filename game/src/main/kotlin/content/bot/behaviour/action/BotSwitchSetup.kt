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
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.slot
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * Equip from inventory the items declared in [equipment]. One slot dispatched per [update] call by
 * default; hybrid loadout swaps pass [maxPerTick] > 1 to commit several slots within the same tick
 * (engine processes each `InteractInterface` synchronously, so subsequent slot scans see the
 * post-swap state).
 *
 * Returns Wait(1, Running) when at least one slot was dispatched, or Success when nothing left
 * to do (all target slots already match, or no inventory candidate exists for the remaining ones).
 */
data class BotSwitchSetup(
    val equipment: Map<EquipSlot, BotItem>,
    val condition: Condition? = null,
    val maxPerTick: Int = 1,
) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        if (condition != null && !condition.check(player)) {
            return BehaviourState.Success
        }
        var swaps = 0
        while (swaps < maxPerTick) {
            when (swapNextSlot(player, world)) {
                SwapResult.SWAPPED -> swaps++
                SwapResult.NONE -> return if (swaps == 0) BehaviourState.Success else BehaviourState.Wait(1, BehaviourState.Running)
                is SwapResult.Failed -> return BehaviourState.Failed(Reason.Invalid("Equip dispatch failed."))
            }
        }
        return BehaviourState.Wait(1, BehaviourState.Running)
    }

    private fun swapNextSlot(player: Player, world: BotWorld): SwapResult {
        for ((slot, entry) in equipment) {
            val worn = player.equipped(slot)
            if (entry.ids.contains(worn.id)) continue
            val inv = player.inventory
            var invSlot = -1
            var invItemId = -1
            for (index in inv.indices) {
                val item = inv[index]
                if (item.slot != slot) continue
                if (!entry.ids.contains(item.id)) continue
                invSlot = index
                invItemId = item.def.id
                break
            }
            if (invSlot == -1) continue
            return dispatchEquip(player, world, slot, invSlot, invItemId)
        }
        return SwapResult.NONE
    }

    private fun dispatchEquip(player: Player, world: BotWorld, slot: EquipSlot, invSlot: Int, invItemId: Int): SwapResult {
        val componentDef = InterfaceDefinitions.getComponent("inventory", "inventory") ?: return SwapResult.Failed
        val componentId = InterfaceDefinitions.getComponentId("inventory", "inventory") ?: return SwapResult.Failed
        val options = componentDef.options ?: componentDef.getOrNull("options") ?: emptyArray()
        var optionIndex = options.indexOf("Equip")
        if (optionIndex == -1) optionIndex = options.indexOf("Wield")
        if (optionIndex == -1) {
            val def = InterfaceHandler.getInventory(player, "inventory", "inventory", componentDef)
            val candidate = if (def != null) player.inventories.inventory(def)[invSlot].def.options.indexOf("Wield") else -1
            if (candidate == -1) return SwapResult.Failed
            optionIndex = candidate
        }
        val interfaceDef = InterfaceDefinitions.getOrNull("inventory") ?: return SwapResult.Failed
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
        return if (valid) SwapResult.SWAPPED else SwapResult.Failed
    }

    private sealed class SwapResult {
        object SWAPPED : SwapResult()
        object NONE : SwapResult()
        object Failed : SwapResult()
    }
}
