package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.update.task.viewport.Spiral
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.path.algorithm.BresenhamsLine
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isThrowingAxe(weapon: Item?) = weapon != null && (weapon.name == "rune_throwing_axe")

val players: Players by inject()
val npcs: NPCs by inject()
val lineOfSight: BresenhamsLine by inject()

on<CombatSwing>({ player -> !swung() && player.specialAttack && isThrowingAxe(player.weapon) }, Priority.MEDIUM) { player: Player ->
    val speed = player.weapon.def["attack_speed", 4]
    delay = if (player.attackType == "rapid") speed - 1 else speed
    if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 10)) {
        delay = -1
        return@on
    }
    val ammo = player.ammo
    player["chain_hits"] = mutableSetOf(target.index)
    player.setAnimation("rune_throwing_axe_special")
    player.setGraphic("${ammo}_special_throw", height = 100)
    player.shoot(name = "${ammo}_special", target = target, delay = 40, height = 40, endHeight = target.height, curve = 8)
    player.hit(target)
}

on<CombatDamage>({ special && isThrowingAxe(weapon) && target.inMultiCombat }) { player: Player ->
    val chain: MutableSet<Int> = player["chain_hits"]
    Spiral.spiral(target.tile, 4) { tile ->
        (if (target is Player) players[tile] else npcs[tile])?.forEach { character ->
            if (character == null || character == target || chain.contains(character.index) || !canAttack(player, character)) {
                return@forEach
            }
            if (!lineOfSight.withinSight(target.tile, character.tile)) {
                return@forEach
            }
            if (!drainSpecialEnergy(player, MAX_SPECIAL_ATTACK / 10)) {
                player.clear("chain_hits")
                return@on
            }
            chain.add(character.index)
            target.shoot(name = "rune_throwing_axe_special", target = character, delay = 40, flightTime = 30, height = target.height, endHeight = character.height)
            player.hit(character, weapon, type, special = true, delay = 1)
            return@on
        }
    }
}