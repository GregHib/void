package content.area.asgarnia.falador

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel

class Highwayman : Script {

    init {
        huntPlayer("highwayman*", "highwayman") { target ->
            if (target.combatLevel < 11) {
                interactPlayer(target, "Attack")
            }
        }

        npcCombatStart { target ->
            if (id.startsWith("highwayman") && target is Player) {
                say("Stand and deliver!")
            }
        }
    }
}
