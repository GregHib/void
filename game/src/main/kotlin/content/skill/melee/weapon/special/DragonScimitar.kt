package content.skill.melee.weapon.special

import content.skill.prayer.PrayerApi
import content.skill.prayer.getActivePrayerVarKey
import content.skill.prayer.isCurses
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.*
import java.util.concurrent.TimeUnit

class DragonScimitar :
    Script,
    PrayerApi {

    init {
        specialAttackDamage("sever") { target, damage ->
            if (damage >= 0) {
                target.softTimers.start("sever")
            }
        }

        prayerStart("protect_from_summoning", ::activate)
        prayerStart("protect_from_magic", ::activate)
        prayerStart("protect_from_melee", ::activate)
        prayerStart("protect_from_missiles", ::activate)
        prayerStart("deflect_summoning", ::activate)
        prayerStart("deflect_magic", ::activate)
        prayerStart("deflect_melee", ::activate)
        prayerStart("deflect_missiles", ::activate)

        timerStart("sever", ::start)
        timerTick("sever") { Timer.CANCEL }
    }

    fun activate(player: Player, prayer: String) {
        if (player.softTimers.contains("sever")) {
            player.message("You've been injured and can no longer use ${if (player.isCurses()) "deflect curses" else "protection prayers"}!")
            val key = player.getActivePrayerVarKey()
            player.removeVarbit(key, prayer.removePrefix("prayer_").toTitleCase())
        }
    }

    fun start(player: Player, restart: Boolean): Int {
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
}
