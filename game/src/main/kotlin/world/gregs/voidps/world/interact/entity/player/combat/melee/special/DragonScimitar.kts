package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.removeVar
import world.gregs.voidps.engine.entity.EffectStart
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.toTicks
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
        target.start("sever", TimeUnit.SECONDS.toTicks(5))
    }
    delay = 4
}

on<EffectStart>({ effect == "sever" }) { player: Player ->
    val key = player.getActivePrayerVarKey()
    if (player.isCurses()) {
        player.removeVar(key, "Deflect Magic")
        player.removeVar(key, "Deflect Melee")
        player.removeVar(key, "Deflect Missiles")
        player.removeVar(key, "Deflect Summoning")
    } else {
        player.removeVar(key, "Protect from Magic")
        player.removeVar(key, "Protect from Melee")
        player.removeVar(key, "Protect from Missiles")
        player.removeVar(key, "Protect from Summoning")
    }
}

on<EffectStart>({ (effect.startsWith("prayer_deflect") || effect.startsWith("prayer_protect")) && it.hasEffect("sever") }) { player: Player ->
    player.message("You've been injured and can no longer use ${if (player.isCurses()) "deflect curses" else "protection prayers"}!")
    val key = player.getActivePrayerVarKey()
    player.removeVar(key, effect.removePrefix("prayer_").toTitleCase())
}