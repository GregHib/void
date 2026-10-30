package content.area.wilderness.bandit_camp

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class GuardBandit : Script {

    init {
        npcCombatStart { target ->
            if (id == "guard_bandit" && target is Player) {
                say("You shall not pass!")
            }
        }
    }
}
