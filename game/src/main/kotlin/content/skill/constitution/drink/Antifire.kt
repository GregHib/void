package content.skill.constitution.drink

import content.entity.player.effect.antifire
import content.entity.player.effect.superAntifire
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick

@Script
class Antifire {

    init {
        playerSpawn { player ->
            if (player.antifire) {
                player.timers.restart("fire_resistance")
            }
            if (player.superAntifire) {
                player.timers.restart("fire_immunity")
            }
        }

        timerStart("fire_resistance") {
            interval = 30
        }

        timerStart("fire_immunity") {
            interval = 20
        }

        timerTick("fire_resistance", "fire_immunity") { player ->
            val remaining = player.dec(if (timer == "fire_immunity") "super_antifire" else "antifire", 0)
            if (remaining <= 0) {
                cancel()
                return@timerTick
            }
            if (remaining == 1) {
                player.message("<dark_red>Your resistance to dragonfire is about to run out.")
            }
        }

        timerStop("fire_resistance", "fire_immunity") { player ->
            player.message("<dark_red>Your resistance to dragonfire has run out.")
            player["antifire"] = 0
            player["super_antifire"] = 0
        }
    }
}
