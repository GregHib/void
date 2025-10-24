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

    override fun spawn(player: Player) {
        if (player.antifire) {
            player.timers.restart("fire_resistance")
        }
        if (player.superAntifire) {
            player.timers.restart("fire_immunity")
        }
    }

    @Key("fire_resistance,fire_immunity")
    override fun start(player: Player, timer: String, restart: Boolean): Int = if (timer == "fire_resistance") 30 else 20

    @Key("fire_resistance,fire_immunity")
    override fun tick(player: Player, timer: String): Int {
        val remaining = player.dec(if (timer == "fire_immunity") "super_antifire" else "antifire", 0)
        if (remaining <= 0) {
            return Timer.CANCEL
        }
        if (remaining == 1) {
            player.message("<dark_red>Your resistance to dragonfire is about to run out.")
        }
        return Timer.CONTINUE
    }

    @Key("fire_resistance,fire_immunity")
    override fun stop(player: Player, timer: String, logout: Boolean) {
        player.message("<dark_red>Your resistance to dragonfire has run out.")
        player["antifire"] = 0
        player["super_antifire"] = 0
    }
}
