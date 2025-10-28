package content.skill.constitution.drink

import content.entity.player.effect.antifire
import content.entity.player.effect.superAntifire
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.*

@Script
class Antifire : Api {

    init {
        playerSpawn { player ->
            if (player.antifire) {
                player.timers.restart("fire_resistance")
            }
            if (player.superAntifire) {
                player.timers.restart("fire_immunity")
            }
        }

        timerStart("fire_resistance") { 30 }
        timerStart("fire_immunity") { 20 }
        timerTick("fire_resistance") { decrease(this, "antifire") }
        timerTick("fire_immunity") { decrease(this, "super_antifire") }
        timerStop("fire_resistance,fire_immunity", ::clear)
    }

    fun decrease(player: Player, key: String): Int {
        val remaining = player.dec(key, 0)
        if (remaining <= 0) {
            return Timer.CANCEL
        }
        if (remaining == 1) {
            player.message("<dark_red>Your resistance to dragonfire is about to run out.")
        }
        return Timer.CONTINUE
    }

    fun clear(player: Player, logout: Boolean) {
        player.message("<dark_red>Your resistance to dragonfire has run out.")
        player["antifire"] = 0
        player["super_antifire"] = 0
    }
}
