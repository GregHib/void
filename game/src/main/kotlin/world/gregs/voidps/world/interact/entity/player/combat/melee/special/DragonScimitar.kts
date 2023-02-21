package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.removeVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.activity.combat.prayer.PrayerStart
import world.gregs.voidps.world.activity.combat.prayer.getActivePrayerVarKey
import world.gregs.voidps.world.activity.combat.prayer.isCurses
import world.gregs.voidps.world.interact.entity.combat.CombatSwing
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.melee.specialAccuracyMultiplier
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import java.util.concurrent.TimeUnit

fun isDragonScimitar(item: Item?) = item != null && item.id.endsWith("dragon_scimitar")

specialAccuracyMultiplier(1.25, ::isDragonScimitar)

on<CombatSwing>({ !swung() && it.specialAttack && isDragonScimitar(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, 550)) {
        delay = -1
        return@on
    }
    player.setAnimation("sever")
    player.setGraphic("sever")
    if (player.hit(target) > 0) {
        target.softTimers.start("sever")
    }
    delay = 4
}

on<TimerStart>({ timer == "sever" }) { player: Player ->
    interval = TimeUnit.SECONDS.toTicks(5)
    val key = player.getActivePrayerVarKey()
    if (player.isCurses()) {
        player.removeVar(key, "deflect_magic")
        player.removeVar(key, "deflect_melee")
        player.removeVar(key, "deflect_missiles")
        player.removeVar(key, "deflect_summoning")
    } else {
        player.removeVar(key, "protect_from_magic")
        player.removeVar(key, "protect_from_melee")
        player.removeVar(key, "protect_from_missiles")
        player.removeVar(key, "protect_from_summoning")
    }
}

on<TimerTick>({ timer == "sever" }) { _: Player ->
    cancel()
}

on<PrayerStart>({ (prayer.startsWith("prayer_deflect") || prayer.startsWith("prayer_protect")) && it.softTimers.contains("sever") }) { player: Player ->
    player.message("You've been injured and can no longer use ${if (player.isCurses()) "deflect curses" else "protection prayers"}!")
    val key = player.getActivePrayerVarKey()
    player.removeVar(key, prayer.removePrefix("prayer_").toTitleCase())
}