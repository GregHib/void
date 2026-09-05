package content.skill.constitution.drink

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural

class Potions : Script {

    init {
        consumed("*") { item, _ ->
            if (!item.def["empty", ""].contains("vial")) {
                return@consumed
            }
            val separator = item.id.lastIndexOf('_')
            if (separator == -1) {
                return@consumed
            }
            val doses = item.id.substring(separator + 1).toIntOrNull()
            if (doses == null || doses !in 1..5) {
                return@consumed
            }
            if (doses > 1) {
                message("You have ${doses - 1} ${"dose".plural(doses - 1)} of the potion left.")
            } else {
                message("You have finished your potion.")
            }
            potionEffects(item.id)
        }
    }
}
