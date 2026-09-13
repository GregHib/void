package content.skill.constitution.drink

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural

class Potions : Script {

    init {
        consumed("*") { item, _ ->
            val doses = item.id.substringAfterLast('_').toIntOrNull()
            if (doses == null || doses !in 1..5) {
                return@consumed
            }
            if (!potionEffects(item.id)) {
                return@consumed
            }
            if (doses > 1) {
                message("You have ${doses - 1} ${"dose".plural(doses - 1)} of the potion left.")
            } else {
                message("You have finished your potion.")
            }
        }
    }
}
