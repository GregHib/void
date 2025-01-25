package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.timer.toTicks
import content.skill.prayer.getActivePrayerVarKey
import content.skill.prayer.isCurses
import content.skill.prayer.prayerStart
import content.entity.player.combat.special.specialAttackHit
import java.util.concurrent.TimeUnit

specialAttackHit("sever") {
    target.softTimers.start(id)
}

timerStart("sever") { player ->
    interval = TimeUnit.SECONDS.toTicks(5)
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
}

timerTick("sever") {
    cancel()
}

prayerStart("prayer_deflect_*", "prayer_protect_*") { player ->
    if (player.softTimers.contains("sever")) {
        player.message("You've been injured and can no longer use ${if (player.isCurses()) "deflect curses" else "protection prayers"}!")
        val key = player.getActivePrayerVarKey()
        player.removeVarbit(key, prayer.removePrefix("prayer_").toTitleCase())
    }
}