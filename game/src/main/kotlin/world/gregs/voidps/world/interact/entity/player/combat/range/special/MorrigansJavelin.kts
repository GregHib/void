package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.combat.hit.directHit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.player.combat.throwHitDelay
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isJavelin(weapon: Item?) = weapon != null && (weapon.id.startsWith("morrigans_javelin"))

on<CombatSwing>({ player -> !swung() && player.fightStyle == "range" && player.specialAttack && isJavelin(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@on
    }
    val ammo = player.ammo
    player.setAnimation("throw_javelin")
    player.setGraphic("${ammo}_special")
    player.shoot(id = ammo, target = target)
    val distance = player.tile.distanceTo(target)
    val damage = player.hit(target, delay = throwHitDelay(distance))
    if (damage != -1) {
        target["phantom_damage"] = damage
        target["phantom"] = player
        target["phantom_first"] = "start"
        target.softTimers.start("phantom_strike")
    }
}

on<TimerStart>({ timer == "phantom_strike" }) { _: Character ->
    interval = 3
}

on<TimerTick>({ timer == "phantom_strike" }) { character: Character ->
    val remaining = character["phantom_damage", 0]
    val damage = remaining.coerceAtMost(50)
    if (remaining - damage <= 0) {
        return@on cancel()
    }
    character["phantom_damage"] = remaining - damage
    val source = character["phantom", character]
    character.directHit(source, damage, "effect")
    (character as? Player)?.message("You ${character.remove("phantom_first") ?: "continue"} to bleed as a result of the javelin strike.")
}

on<TimerStop>({ timer == "phantom_strike" }) { character: NPC ->
    character.clear("phantom")
    character.clear("phantom_damage")
    character.clear("phantom_first")
}
