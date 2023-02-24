package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.variable.containsVarbit
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.sendVariable
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
    player.sendVariable("attack_bonus")
    player.sendVariable("strength_bonus")
    player.sendVariable("defence_bonus")
    player.sendVariable("ranged_bonus")
    player.sendVariable("magic_bonus")
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
    val activePrayers: List<String> = player[key]
    if (activePrayers.isEmpty() && player.softTimers.contains("prayer_drain")) {
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
        containsVarbit(ACTIVE_CURSES, "wrath") -> value = 19
        containsVarbit(ACTIVE_CURSES, "soul_split") -> value = 20
        else -> {
            if (containsVarbit(ACTIVE_CURSES, "deflect_summoning")) {
                value += 4
            }

            value += when {
                containsVarbit(ACTIVE_CURSES, "deflect_magic") -> if (value > -1) 3 else 2
                containsVarbit(ACTIVE_CURSES, "deflect_missiles") -> if (value > -1) 2 else 3
                containsVarbit(ACTIVE_CURSES, "deflect_melee") -> 1
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
        containsVarbit(ACTIVE_PRAYERS, "retribution") -> value = 3
        containsVarbit(ACTIVE_PRAYERS, "redemption") -> value = 5
        containsVarbit(ACTIVE_PRAYERS, "smite") -> value = 4
        else -> {
            if (containsVarbit(ACTIVE_PRAYERS, "protect_from_summoning")) {
                value += 8
            }

            value += when {
                containsVarbit(ACTIVE_PRAYERS, "protect_from_magic") -> 3
                containsVarbit(ACTIVE_PRAYERS, "protect_from_missiles") -> 2
                containsVarbit(ACTIVE_PRAYERS, "protect_from_melee") -> 1
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