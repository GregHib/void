package content.area.asgarnia.falador

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class Highwayman : Script {

    init {
        npcCombatStart { target ->
            if (id.startsWith("highwayman") && target is Player) {
                say("Stand and deliver!")
            }
        }
    }
}
