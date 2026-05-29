package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.condition.Condition
import content.entity.player.combat.special.specialAttack
import content.entity.player.combat.special.specialAttackEnergy
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

data class BotSpecAttack(
    val weapon: String,
    val fallback: String,
    val minEnergy: Int = 250,
    val condition: Condition? = null,
) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        val worn = player.equipped(EquipSlot.Weapon).id
        val queued = player.specialAttack
        val sameWeapon = weapon == fallback

        if (!sameWeapon && worn == weapon && !queued) {
            equipFromInventory(bot, world, fallback)
            return BehaviourState.Success
        }

        if (queued) return BehaviourState.Success
        if (condition != null && !condition.check(player)) return BehaviourState.Success
        if (player.specialAttackEnergy < minEnergy) return BehaviourState.Success

        if (worn == weapon) {
            player.specialAttack = true
        } else if (worn == fallback) {
            if (equipFromInventory(bot, world, weapon)) {
                player.specialAttack = true
            }
        }
        return BehaviourState.Success
    }

    private fun equipFromInventory(bot: Bot, world: BotWorld, itemId: String): Boolean {
        val player = bot.player
        val inv = player.inventory
        var invSlot = -1
        var invItemId = -1
        for (index in inv.indices) {
            val item = inv[index]
            if (item.id == itemId) {
                invSlot = index
                invItemId = item.def.id
                break
            }
        }
        if (invSlot == -1) return false
        val componentDef = InterfaceDefinitions.getComponent("inventory", "inventory") ?: return false
        val componentId = InterfaceDefinitions.getComponentId("inventory", "inventory") ?: return false
        val options = componentDef.options ?: componentDef.getOrNull("options") ?: emptyArray()
        var optionIndex = options.indexOf("Equip")
        if (optionIndex == -1) optionIndex = options.indexOf("Wield")
        if (optionIndex == -1) return false
        val interfaceDef = InterfaceDefinitions.getOrNull("inventory") ?: return false
        return world.execute(
            player,
            InteractInterface(
                interfaceId = interfaceDef.id,
                componentId = componentId,
                itemId = invItemId,
                itemSlot = invSlot,
                option = optionIndex,
            ),
        )
    }
}
