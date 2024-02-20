package world.gregs.voidps.world.interact.entity.player.combat.melee.special

import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.*
import world.gregs.voidps.world.interact.entity.combat.Target
import world.gregs.voidps.world.interact.entity.combat.hit.Damage
import world.gregs.voidps.world.interact.entity.combat.hit.block
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.hit.specialAttackHit
import world.gregs.voidps.world.interact.entity.player.combat.special.drainSpecialEnergy

weaponSwing("korasis_sword", Priority.LOW) { player ->
    player.setAnimation("korasis_sword_${
        when (player.attackType) {
            "chop" -> "chop"
            else -> "slash"
        }
    }")
    player.hit(target)
    delay = 5
}

block("korasis_sword") {
    target.setAnimation("korasis_sword_block", delay)
    blocked = true
}

// Special attack

val players: Players by inject()
val npcs: NPCs by inject()
val lineOfSight: LineValidator by inject()

specialAttackSwing("korasis_sword") { player ->
    if (!drainSpecialEnergy(player, 600)) {
        delay = -1
        return@specialAttackSwing
    }
    player["korasi_chain"] = mutableSetOf(target.index)
    player.setAnimation("disrupt")
    player.setGraphic("disrupt")
    val maxHit = Damage.maximum(player, target, "melee", player.weapon)
    val hit = random.nextInt(maxHit / 2, (maxHit * 1.5).toInt())
    player.hit(target, damage = hit, type = "magic", delay = 0)
    delay = 5
}

specialAttackHit("korasis_sword") { character ->
    character.setGraphic("disrupt_hit")
}

specialAttackHit("korasis_sword") { target ->
    if (!target.inMultiCombat) {
        return@specialAttackHit
    }
    val chain: MutableSet<Int> = source["korasi_chain", mutableSetOf()]
    if (chain.size >= 3) {
        return@specialAttackHit
    }
    val characters = if (target is Player) players else npcs
    for (tile in target.tile.spiral(4)) {
        characters[tile].forEach { character ->
            if (character == target || chain.contains(character.index) || !Target.attackable(source, character)) {
                return@forEach
            }
            if (!lineOfSight.hasLineOfSight(target, character)) {
                return@forEach
            }
            chain.add(character.index)
            val hit = damage / when (chain.size) {
                2 -> 2
                3 -> 4
                else -> return@specialAttackHit
            }
            source.hit(character, damage = hit, weapon = weapon, type = type, special = true)
            return@specialAttackHit
        }
    }
}