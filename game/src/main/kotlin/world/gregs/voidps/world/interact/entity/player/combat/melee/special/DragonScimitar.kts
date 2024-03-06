package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.prayer.getActivePrayerVarKey
import world.gregs.voidps.world.interact.entity.player.combat.prayer.isCurses
import world.gregs.voidps.world.interact.entity.player.combat.prayer.prayerStart
import java.util.concurrent.TimeUnit

combatSwing("*dragon_scimitar", "melee", special = true) { player ->
    player.setAnimation("sever")
    player.setGraphic("sever")
    if (player.hit(target) > 0) {
        target.softTimers.start("sever")
    }
    delay = 4
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