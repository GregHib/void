package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.timer.characterTimerStart
import world.gregs.voidps.engine.timer.characterTimerTick
import world.gregs.voidps.engine.timer.npcTimerStop
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.directHit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.specialAttackSwing
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.proj.shoot

specialAttackSwing("morrigans_javelin*", style = "range", priority = Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 2)) {
        delay = -1
        return@specialAttackSwing
    }
    val ammo = player.ammo
    player.setAnimation("throw_javelin")
    player.setGraphic("${ammo}_special")
    player.shoot(id = ammo, target = target)
    val distance = player.tile.distanceTo(target)
    val damage = player.hit(target, delay = Hit.throwDelay(distance))
    if (damage != -1) {
        target["phantom_damage"] = damage
        target["phantom"] = player
        target["phantom_first"] = "start"
        target.softTimers.start("phantom_strike")
    }
}

characterTimerStart("phantom_strike") { _: Character ->
    interval = 3
}

characterTimerTick("phantom_strike") { character: Character ->
    val remaining = character["phantom_damage", 0]
    val damage = remaining.coerceAtMost(50)
    if (remaining - damage <= 0) {
        cancel()
        return@characterTimerTick
    }
    character["phantom_damage"] = remaining - damage
    val source = character["phantom", character]
    character.directHit(source, damage, "effect")
    (character as? Player)?.message("You ${character.remove("phantom_first") ?: "continue"} to bleed as a result of the javelin strike.")
}

npcTimerStop("phantom_strike") { npc: NPC ->
    npc.clear("phantom")
    npc.clear("phantom_damage")
    npc.clear("phantom_first")
}
