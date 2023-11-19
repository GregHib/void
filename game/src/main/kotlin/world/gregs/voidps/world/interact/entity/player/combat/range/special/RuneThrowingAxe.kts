package world.gregs.voidps.world.interact.entity.player.combat.range.special

import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.combat.Target
import world.gregs.voidps.world.interact.entity.combat.hit.CombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.player.combat.special.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.combat.special.specialAttack
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isThrowingAxe(weapon: Item?) = weapon != null && (weapon.id == "rune_throwing_axe")

val players: Players by inject()
val npcs: NPCs by inject()
val lineOfSight: LineValidator by inject()

on<CombatSwing>({ player -> !swung() && player.fightStyle == "range" && player.specialAttack && isThrowingAxe(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 10)) {
        delay = -1
        return@on
    }
    val ammo = player.ammo
    player["chain_hits"] = mutableSetOf(target.index)
    player.setAnimation("rune_throwing_axe_special")
    player.setGraphic("${ammo}_special_throw")
    player.shoot(id = "${ammo}_special", target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.throwDelay(distance))
}

on<CombatHit>({ target -> source is Player && special && isThrowingAxe(weapon) && target.inMultiCombat }) { target: Character ->
    val player = source as Player
    val chain: MutableSet<Int> = player["chain_hits"]
    val characters = if (target is Player) players else npcs
    for (tile in target.tile.spiral(4)) {
        characters[tile].forEach { character ->
            if (character == target || chain.contains(character.index) || !Target.attackable(player, character)) {
                return@forEach
            }
            if (!lineOfSight.hasLineOfSight(target, character)) {
                return@forEach
            }
            if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 10)) {
                player.clear("chain_hits")
                return@on
            }
            chain.add(character.index)
            target.shoot(id = "rune_throwing_axe_special", target = character)
            player.hit(character, weapon!!, type, special = true, delay = 1)
            return@on
        }
    }
}