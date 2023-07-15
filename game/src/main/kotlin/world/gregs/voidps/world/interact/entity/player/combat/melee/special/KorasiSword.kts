package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.character.size
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.player.combat.drainSpecialEnergy
import world.gregs.voidps.world.interact.entity.player.combat.specialAttack
import kotlin.random.Random

fun isKorasisSword(item: Item?) = item != null && item.id == "korasis_sword"

on<CombatSwing>({ !swung() && isKorasisSword(it.weapon) }, Priority.LOW) { player: Player ->
    player.setAnimation("korasis_sword_${
        when (player.attackType) {
            "chop" -> "chop"
            else -> "slash"
        }
    }")
    player.hit(target)
    delay = 5
}

on<CombatAttack>({ !blocked && target is Player && isKorasisSword(target.weapon) }) { _: Character ->
    target.setAnimation("korasis_sword_block", delay)
    blocked = true
}

// Special attack

val players: Players by inject()
val npcs: NPCs by inject()
val lineOfSight: LineValidator by inject()

on<HitChanceModifier>({ type == "magic" && special && isKorasisSword(weapon) }, Priority.HIGHEST) { _: Player ->
    chance = 1.0
}

on<CombatSwing>({ !swung() && it.specialAttack && isKorasisSword(it.weapon) }) { player: Player ->
    if (!drainSpecialEnergy(player, 600)) {
        delay = -1
        return@on
    }
    player["korasi_chain"] = mutableSetOf(target.index)
    player.setAnimation("disrupt")
    player.setGraphic("disrupt")
    val maxHit = getMaximumHit(player, target, "melee", player.weapon, special = true)
    val hit = Random.nextInt(maxHit / 2, (maxHit * 1.5).toInt())
    player.hit(target, damage = hit, type = "magic", delay = 0)
    delay = 5
}

on<CombatHit>({ special && isKorasisSword(weapon) }) { character: Character ->
    character.setGraphic("disrupt_hit")
}

on<CombatHit>({ target -> special && isKorasisSword(weapon) && target.inMultiCombat }) { target: Character ->
    val chain: MutableSet<Int> = source["korasi_chain", mutableSetOf()]
    if (chain.size >= 3) {
        return@on
    }
    val characters = if (target is Player) players else npcs
    for (tile in target.tile.spiral(4)) {
        characters[tile].forEach { character ->
            if (character == target || chain.contains(character.index) || !canAttack(source, character)) {
                return@forEach
            }
            if (!lineOfSight.hasLineOfSight(
                    srcX = target.tile.x,
                    srcZ = target.tile.y,
                    level = target.tile.level,
                    destX = character.tile.x,
                    destZ = character.tile.y,
                    srcSize = target.size,
                    destWidth = character.size,
                    destHeight = character.size)
            ) {
                return@forEach
            }
            chain.add(character.index)
            val hit = damage / when (chain.size) {
                2 -> 2
                3 -> 4
                else -> return@on
            }
            source.hit(character, damage = hit, weapon = weapon, type = type, special = true)
            return@on
        }
    }
}