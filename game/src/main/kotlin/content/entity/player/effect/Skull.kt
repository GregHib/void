package content.entity.player.effect

import content.area.wilderness.inWilderness
import world.gregs.voidps.engine.entity.character.mode.combat.CombatApi
import content.entity.combat.attackers
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.timer.*
import java.util.concurrent.TimeUnit

val Player.skulled: Boolean get() = skullCounter > 0

var Player.skullCounter: Int
    get() = get("skull_duration", 0)
    set(value) = set("skull_duration", value)

fun Player.skull(minutes: Int = 10, type: Int = 0) {
    set("skull", type)
    skullCounter = TimeUnit.MINUTES.toTicks(minutes) / 50
    softTimers.start("skull")
}

fun Player.unskull() {
    clear("skull")
    skullCounter = 0
    softTimers.stop("skull")
}

class Skull : Script {

    init {
        playerSpawn {
            if (skulled) {
                softTimers.restart("skull")
            }
        }

        combatStart { target ->
            if (inWilderness && target is Player && !attackers.contains(target)) {
                skull()
            }
        }

        timerStart("skull") {
            appearance.skull = this["skull", 0]
            flagAppearance()
            50
        }

        timerTick("skull") {
            if (--skullCounter <= 0) Timer.CANCEL else Timer.CONTINUE
        }

        timerStop("skull") {
            clear("skull")
            clear("skull_duration")
            appearance.skull = -1
            flagAppearance()
        }
    }
}
