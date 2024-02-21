package world.gregs.voidps.world.interact.entity.player.combat.range.special

import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.combat.Target
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.hit.specialAttackHit
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.proj.shoot

val players: Players by inject()
val npcs: NPCs by inject()
val lineOfSight: LineValidator by inject()

specialAttackSwing("rune_throwing_axe", style = "range", priority = Priority.MEDIUM) { player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 10)) {
        delay = -1
        return@specialAttackSwing
    }
    val ammo = player.ammo
    player["chain_hits"] = mutableSetOf(target.index)
    player.setAnimation("rune_throwing_axe_special")
    player.setGraphic("${ammo}_special_throw")
    player.shoot(id = "${ammo}_special", target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.throwDelay(distance))
}

specialAttackHit("rune_throwing_axe", "range") { target ->
    if (source !is Player || !target.inMultiCombat) {
        return@specialAttackHit
    }
    val chain: MutableSet<Int> = source.getOrPut("chain_hits") { mutableSetOf() }
    val characters = if (target is Player) players else npcs
    for (tile in target.tile.spiral(4)) {
        characters[tile].forEach { character ->
            if (character == target || chain.contains(character.index) || !Target.attackable(source, character)) {
                return@forEach
            }
            if (!lineOfSight.hasLineOfSight(target, character)) {
                return@forEach
            }
            if (!drainSpecialEnergy(source, MAX_SPECIAL_ATTACK / 10)) {
                source.clear("chain_hits")
                return@specialAttackHit
            }
            chain.add(character.index)
            target.shoot(id = "rune_throwing_axe_special", target = character)
            source.hit(character, weapon, type, special = true, delay = 1)
            return@specialAttackHit
        }
    }
}