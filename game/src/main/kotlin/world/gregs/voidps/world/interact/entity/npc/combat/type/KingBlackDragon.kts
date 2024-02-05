package world.gregs.voidps.world.interact.entity.npc.combat.type

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.target.CharacterTargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.size
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.effect.freeze
import world.gregs.voidps.world.interact.entity.player.toxin.poison
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

val specials = listOf("toxic", "ice", "shock")

combatSwing({ npc -> !swung() && npc.id == "king_black_dragon" }, Priority.HIGHEST) { npc: NPC ->
    val canMelee = CharacterTargetStrategy(npc).reached(target)
    when (random.nextInt(if (canMelee) 3 else 2)) {
        0 -> {
            npc.setAnimation("dragon_breath")
            target.playSound("dragon_breath")
            nearestTile(npc, target).shoot("dragon_breath", target)
            npc.hit(target, type = "dragonfire")
        }
        1 -> {
            val type = specials.random()
            npc.setAnimation("dragon_breath")
            target.playSound("dragon_breath_$type")
            nearestTile(npc, target).shoot("dragon_breath_$type", target)
            npc.hit(target, type = "dragonfire", spell = type, special = true)
        }
        else -> {
            npc.setAnimation("dragon_attack")
            target.playSound("dragon_attack")
            npc.hit(target, type = "melee")
        }
    }
    delay = npc.def["attack_speed", 4]
}

/**
 * Tile the dragon breath originates from.
 * Looks weird imo, but it's the same as OSRS.
 */
fun nearestTile(source: Character, target: Character): Tile {
    val half = source.size / 2
    val centre = source.tile.add(half, half)
    val direction = target.tile.delta(centre).toDirection()
    return centre.add(direction).add(direction)
}

combatAttack({ it.id == "king_black_dragon" }) { npc: NPC ->
    when (spell) {
        "toxic" -> npc.poison(target, 80)
        "ice" -> npc.freeze(target, 10)
        "shock" -> {
            target.message("You're shocked and weakened!")
            for (skill in Skill.all) {
                target.levels.drain(skill, 2)
            }
        }
    }
}