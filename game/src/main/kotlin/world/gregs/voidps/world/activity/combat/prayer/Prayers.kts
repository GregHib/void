package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.hasVar
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.flagAppearance
import world.gregs.voidps.engine.entity.character.player.headIcon
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_CURSES
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_PRAYERS
import world.gregs.voidps.world.interact.entity.sound.playSound

on<Registered> { player: Player ->
    player.sendVar("attack_bonus")
    player.sendVar("strength_bonus")
    player.sendVar("defence_bonus")
    player.sendVar("ranged_bonus")
    player.sendVar("magic_bonus")
}

on<PrayerStart> { player: Player ->
    if (!restart) {
        val curses = player.isCurses()
        if (curses) {
            player.setAnimation("activate_$prayer")
            player.setGraphic("activate_$prayer")
        } else {
            player.playSound("activate_$prayer")
        }
        updateOverheadIcon(player, curses)
    }
    player.softTimers.startIfAbsent("prayer_drain")
}

on<PrayerStop> { player: Player ->
    player.playSound("deactivate_prayer")
    val curses = player.isCurses()
    stopPrayerDrain(player, curses)
    updateOverheadIcon(player, curses)
}

fun stopPrayerDrain(player: Player, curses: Boolean) {
    val key = if (curses) ACTIVE_CURSES else ACTIVE_PRAYERS
    val activePrayers = player.getVar(key, emptyList<String>())
    if (activePrayers.isEmpty()) {
        player.softTimers.stop("prayer_drain")
    }
}

fun updateOverheadIcon(player: Player, curses: Boolean) {
    val changed = if (curses) {
        player.changedCurseIcon()
    } else {
        player.changedPrayerIcon()
    }
    if (changed) {
        player.flagAppearance()
    }
}

fun Player.changedCurseIcon(): Boolean {
    var value = -1
    when {
        hasVar(ACTIVE_CURSES, "Wrath") -> value = 19
        hasVar(ACTIVE_CURSES, "Soul Split") -> value = 20
        else -> {
            if (hasVar(ACTIVE_CURSES, "Deflect Summoning")) {
                value += 4
            }

            value += when {
                hasVar(ACTIVE_CURSES, "Deflect Magic") -> if (value > -1) 3 else 2
                hasVar(ACTIVE_CURSES, "Deflect Missiles") -> if (value > -1) 2 else 3
                hasVar(ACTIVE_CURSES, "Deflect Melee") -> 1
                else -> 0
            }
            if (value > -1) {
                value += 12
            }
        }
    }
    if (headIcon != value) {
        headIcon = value
        return true
    }
    return false
}

fun Player.changedPrayerIcon(): Boolean {
    var value = -1
    when {
        hasVar(ACTIVE_PRAYERS, "Retribution") -> value = 3
        hasVar(ACTIVE_PRAYERS, "Redemption") -> value = 5
        hasVar(ACTIVE_PRAYERS, "Smite") -> value = 4
        else -> {
            if (hasVar(ACTIVE_PRAYERS, "Protect from Summoning")) {
                value += 8
            }

            value += when {
                hasVar(ACTIVE_PRAYERS, "Protect from Magic") -> 3
                hasVar(ACTIVE_PRAYERS, "Protect from Missiles") -> 2
                hasVar(ACTIVE_PRAYERS, "Protect from Melee") -> 1
                else -> 0
            }
        }
    }

    if (headIcon != value) {
        headIcon = value
        return true
    }
    return false
}