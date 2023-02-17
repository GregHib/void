package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.engine.timer.softTimer
import world.gregs.voidps.engine.timer.stopSoftTimer
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.player.combat.throwHitDelay
import world.gregs.voidps.world.interact.entity.proj.shoot
import kotlin.math.max

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
        target.softTimer("phantom_strike", 3)
    }
}

on<TimerTick>({ timer == "phantom_strike" }) { character: Character ->
    val damage = max(50, character["phantom_damage", 0])
    if (damage <= 0) {
        character.stopSoftTimer(timer)
        return@on
    }
    hit(character["phantom", character], character, damage, "effect")
    if (character is Player) {
//        if (tick == 0L) { FIXME
            character.message("You start to bleed as a result of the javelin strike.")
//        } else {
            character.message("You continue to bleed as a result of the javelin strike.")
//        }
    }
}

on<TimerStop>({ timer == "phantom_strike" }) { character: NPC ->
    character.clear("phantom")
    character.clear("phantom_damage")
}
