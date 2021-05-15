package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.hasVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.flagAppearance
import world.gregs.voidps.engine.entity.character.update.visual.player.headIcon
import world.gregs.voidps.engine.entity.has
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.entity.stop
import world.gregs.voidps.engine.event.on

on<UpdatePrayers> { player: Player ->
    val key = if (player.isCurses()) PrayerConfigs.ACTIVE_CURSES else PrayerConfigs.ACTIVE_PRAYERS
    //TODO update stats
    player.updateOverhead(key)
    updatePrayerDrain(player, key)
}

fun updatePrayerDrain(player: Player, listKey: String) {
    val activePrayers = player.getVar(listKey, 0)
    if (activePrayers == 0 && player.has("prayer_drain")) {
        player.stop("prayer_drain")
    } else if (activePrayers > 0 && !player.has("prayer_drain")) {
        player.start("prayer_drain")
    }
}

fun Player.updateOverhead(listKey: String) {
    val changed = if (isCurses()) {
        setCurseIcon(listKey)
    } else {
        setPrayerIcon(listKey)
    }
    if (changed) {
        flagAppearance()
    }
}

fun Player.setCurseIcon(listKey: String): Boolean {
    var value = -1
    when {
        hasVar(listKey, "Wrath") -> value = 19
        hasVar(listKey, "Soul Split") -> value = 20
        else -> {
            if (hasVar(listKey, "Deflect Summoning")) {
                value += 4
            }

            value += when {
                hasVar(listKey, "Deflect Magic") -> if (value > -1) 3 else 2
                hasVar(listKey, "Deflect Missiles") -> if (value > -1) 2 else 3
                hasVar(listKey, "Deflect Melee") -> 1
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

fun Player.setPrayerIcon(listKey: String): Boolean {
    var value = -1
    when {
        hasVar(listKey, "Retribution") -> value = 3
        hasVar(listKey, "Redemption") -> value = 5
        hasVar(listKey, "Smite") -> value = 4
        else -> {
            if (hasVar(listKey, "Protect from Summoning")) {
                value += 8
            }

            value += when {
                hasVar(listKey, "Protect from Magic") -> 3
                hasVar(listKey, "Protect from Missiles") -> 2
                hasVar(listKey, "Protect from Melee") -> 1
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