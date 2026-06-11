package content.skill.constitution.drink

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Potions : Script {

    init {
        consumed("*") { item, slot ->
            if (!item.id.endsWith("_1") && !item.id.endsWith("_2") && !item.id.endsWith("_3") && !item.id.endsWith("_4")) {
                return@consumed
            }
            val doses = item.id.last().digitToInt()
            if (doses != 1) {
                message("You have ${doses - 1} ${"dose".plural(doses - 1)} of the potion left.")
                potionEffects(item.id)
                return@consumed
            }
            message("You have finished your potion.")
            if (contains("smash_vials")) {
                inventory.remove(slot, item.id)
                message("You quickly smash the empty vial using the tick a Barbarian taught you.")
            }
            potionEffects(item.id)
        }
    }
}
