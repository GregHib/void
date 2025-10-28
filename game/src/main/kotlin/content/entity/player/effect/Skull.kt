package content.entity.player.effect

import content.area.wilderness.inWilderness
import content.entity.combat.attackers
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.character.mode.combat.combatStart
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.event.Script
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

@Script
class Skull : Api {

    @Timer("skull")
    override fun start(player: Player, timer: String, restart: Boolean): Int {
        player.appearance.skull = player["skull", 0]
        player.flagAppearance()
        return 50
    }

    @Timer("skull")
    override fun tick(player: Player, timer: String): Int {
        if (--player.skullCounter <= 0) {
            return Timer.CANCEL
        }
        return Timer.CONTINUE
    }

    @Timer("skull")
    override fun stop(player: Player, timer: String, logout: Boolean) {
        player.clear("skull")
        player.clear("skull_duration")
        player.appearance.skull = -1
        player.flagAppearance()
    }

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
    }
}
