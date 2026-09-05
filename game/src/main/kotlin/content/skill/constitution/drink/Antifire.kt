package content.skill.constitution.drink

import content.entity.player.effect.antifire
import content.entity.player.effect.superAntifire
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.*

class Antifire : Script {

    init {
        playerSpawn {
            if (antifire) {
                timers.restart("fire_resistance")
            }
            if (superAntifire) {
                timers.restart("fire_immunity")
            }
        }

        playerDeath {
            clear("antifire")
            clear("super_antifire")
        }

        timerStart("fire_resistance") { 30 }
        timerStart("fire_immunity") { 20 }
        timerTick("fire_resistance") { decrease(this, "antifire") }
        timerTick("fire_immunity") { decrease(this, "super_antifire") }
        timerStop("fire_resistance") { logout -> expire(this, "antifire", logout) }
        timerStop("fire_immunity") { logout -> expire(this, "super_antifire", logout) }
    }

    fun decrease(player: Player, key: String): Int {
        val remaining = player.dec(key)
        if (remaining <= 0) {
            return Timer.CANCEL
        }
        if (remaining == 1) {
            player.message("<dark_red>Your resistance to dragonfire is about to run out.")
        }
        return Timer.CONTINUE
    }

    fun expire(player: Player, key: String, logout: Boolean) {
        if (logout) {
            return
        }
        player.message("<dark_red>Your resistance to dragonfire has run out.")
        player.clear(key)
    }
}
