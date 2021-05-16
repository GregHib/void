package world.gregs.voidps.world.activity.combat.prayer

import world.gregs.voidps.engine.client.variable.*
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.flagAppearance
import world.gregs.voidps.engine.entity.character.update.visual.player.headIcon
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.has
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.entity.stop
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_CURSES
import world.gregs.voidps.world.activity.combat.prayer.PrayerConfigs.ACTIVE_PRAYERS
import world.gregs.voidps.world.interact.entity.sound.playSound

IntVariable(6857, Variable.Type.VARBIT, defaultValue = 30).register("attack_bonus")
IntVariable(6858, Variable.Type.VARBIT, defaultValue = 30).register("strength_bonus")
IntVariable(6859, Variable.Type.VARBIT, defaultValue = 30).register("defence_bonus")
IntVariable(6860, Variable.Type.VARBIT, defaultValue = 30).register("range_bonus")
IntVariable(6861, Variable.Type.VARBIT, defaultValue = 30).register("magic_bonus")

BooleanVariable(6839, Variable.Type.VARBIT).register("being_leeched")
IntVariable(6844, Variable.Type.VARBIT).register("leech_attack_bonus")
IntVariable(6845, Variable.Type.VARBIT).register("leech_strength_bonus")
IntVariable(6846, Variable.Type.VARBIT).register("leech_defence_bonus")

on<Registered> { player: Player ->
    player.sendVar("attack_bonus")
    player.sendVar("strength_bonus")
    player.sendVar("defence_bonus")
    player.sendVar("range_bonus")
    player.sendVar("magic_bonus")
}

on<PrayerActivate> { player: Player ->
    val id = prayer.replace(" ", "_").toLowerCase()
    if (curses) {
        player.setAnimation("activate_$id")
        player.setGraphic("activate_$id")
    } else {
        player.playSound("activate_$id")
    }
}

on<PrayerDeactivate> { player: Player ->
    player.playSound("deactivate_prayer")
}

on<UpdatePrayers> { player: Player ->
    val key = if (player.isCurses()) ACTIVE_CURSES else ACTIVE_PRAYERS
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