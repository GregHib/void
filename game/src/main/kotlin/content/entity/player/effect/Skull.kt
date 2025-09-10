package content.entity.player.effect

import content.area.wilderness.inWilderness
import content.entity.combat.attackers
import world.gregs.voidps.engine.entity.character.mode.combat.combatStart
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.event.Script

import world.gregs.voidps.engine.timer.toTicks
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
@Script
class Skull {

    init {
        playerSpawn { player ->
            if (player.skulled) {
                player.softTimers.restart("skull")
            }
        }

        combatStart { player ->
            if (player.inWilderness && target is Player && !player.attackers.contains(target)) {
                player.skull()
            }
        }

        timerStart("skull") { player ->
            interval = 50
            player.appearance.skull = player["skull", 0]
            player.flagAppearance()
        }

        timerTick("skull") { player ->
            if (--player.skullCounter <= 0) {
                cancel()
                return@timerTick
            }
        }

        timerStop("skull") { player ->
            player.clear("skull")
            player.clear("skull_duration")
            player.appearance.skull = -1
            player.flagAppearance()
        }

    }

}
