package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.update.task.viewport.spiral
import world.gregs.voidps.engine.entity.character.Character
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
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import world.gregs.voidps.world.interact.entity.player.combat.throwHitDelay
import world.gregs.voidps.world.interact.entity.proj.shoot

fun isThrowingAxe(weapon: Item?) = weapon != null && (weapon.id == "rune_throwing_axe")

val players: Players by inject()
val npcs: NPCs by inject()
val lineOfSight: BresenhamsLine by inject()

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
    player.hit(target, delay = throwHitDelay(distance))
}

on<CombatHit>({ target -> source is Player && special && isThrowingAxe(weapon) && target.inMultiCombat }) { target: Character ->
    val player = source as Player
    val chain: MutableSet<Int> = player["chain_hits"]
    val characters = if (target is Player) players else npcs
    for (tile in target.tile.spiral(4)) {
        characters[tile].forEach { character ->
            if (character == target || chain.contains(character.index) || !canAttack(player, character)) {
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
            target.shoot(id = "rune_throwing_axe_special", target = character)
            player.hit(character, weapon, type, special = true, delay = 1)
            return@on
        }
    }
}