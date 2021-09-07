package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.hasVar
import world.gregs.voidps.engine.client.variable.sendVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.flagAppearance
import world.gregs.voidps.engine.entity.character.update.visual.player.headIcon
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_CURSES
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_PRAYERS
import world.gregs.voidps.world.interact.entity.sound.playSound

on<Registered> { player: Player ->
    player.sendVar("attack_bonus")
    player.sendVar("strength_bonus")
    player.sendVar("defence_bonus")
    player.sendVar("range_bonus")
    player.sendVar("magic_bonus")
}

on<EffectStart>({ effect.startsWith("prayer_") }) { player: Player ->
    val id = effect.removePrefix("prayer_")
    val curses = player.isCurses()
    if (curses) {
        player.setAnimation("activate_$id")
        player.setGraphic("activate_$id")
    } else {
        player.playSound("activate_$id")
    }
    player.hasOrStart("prayer_drain")
    updateOverheadIcon(player, curses)
}

on<EffectStop>({ effect.startsWith("prayer_") }) { player: Player ->
    player.playSound("deactivate_prayer")
    val curses = player.isCurses()
    stopPrayerDrain(player, curses)
    updateOverheadIcon(player, curses)
}

fun stopPrayerDrain(player: Player, curses: Boolean) {
    val key = if (curses) ACTIVE_CURSES else ACTIVE_PRAYERS
    val activePrayers = player.getVar(key, 0)
    if (activePrayers == 0 && player.hasEffect("prayer_drain")) {
        player.stop("prayer_drain")
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