package content.skill.melee.weapon.special

import content.entity.player.combat.special.specialAttackDamage
import content.skill.prayer.getActivePrayerVarKey
import content.skill.prayer.isCurses
import content.skill.prayer.prayerStart
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.timer.*
import java.util.concurrent.TimeUnit

@Script
class DragonScimitar : Api {

    @Key("sever")
    override fun start(player: Player, timer: String, restart: Boolean): Int {
        val key = player.getActivePrayerVarKey()
        if (player.isCurses()) {
            player.removeVarbit(key, "deflect_magic")
            player.removeVarbit(key, "deflect_melee")
            player.removeVarbit(key, "deflect_missiles")
            player.removeVarbit(key, "deflect_summoning")
        } else {
            player.removeVarbit(key, "protect_from_magic")
            player.removeVarbit(key, "protect_from_melee")
            player.removeVarbit(key, "protect_from_missiles")
            player.removeVarbit(key, "protect_from_summoning")
        }
        return TimeUnit.SECONDS.toTicks(5)
    }

    @Key("sever")
    override fun tick(player: Player, timer: String) = Timer.CANCEL

    init {
        specialAttackDamage("sever") {
            target.softTimers.start(id)
        }

        prayerStart("prayer_deflect_*", "prayer_protect_*") { player ->
            if (player.softTimers.contains("sever")) {
                player.message("You've been injured and can no longer use ${if (player.isCurses()) "deflect curses" else "protection prayers"}!")
                val key = player.getActivePrayerVarKey()
                player.removeVarbit(key, prayer.removePrefix("prayer_").toTitleCase())
            }
        }
    }
}
