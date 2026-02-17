package content.bot.behaviour.condition

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

data class BotEquipmentSetup(val items: Map<EquipSlot, BotItem>) : Condition(90) {
    override fun keys() = items.values.flatMap { entry -> entry.ids.map { "item:$it" } }.toSet()
    override fun events() = setOf("inv:worn_equipment")

    override fun check(player: Player): Boolean {
        for ((slot, entry) in items) {
            val item = player.equipped(slot)
            if (entry.ids.contains("empty")) {
                if (item.isEmpty()) {
                    continue
                }
                return false
            }
            if (!entry.ids.contains(item.id)) {
                return false
            }
            if (!inRange(item.amount, entry.min, entry.max)) {
                return false
            }
            if (entry.usable && item.amount > 0 && !player.hasRequirementsToUse(item)) {
                return false
            }
        }
        return true
    }
}