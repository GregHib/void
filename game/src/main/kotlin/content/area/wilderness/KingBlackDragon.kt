package content.area.wilderness

import content.entity.combat.hit.hit
import content.entity.combat.hit.npcCombatAttack
import content.entity.combat.npcCombatSwing
import content.entity.effect.freeze
import content.entity.effect.toxin.poison
import content.entity.proj.shoot
import content.entity.sound.sound
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.target.CharacterTargetStrategy
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class KingBlackDragon : Script {

    val specials = listOf("toxic", "ice", "shock")

    init {
        npcCombatSwing("king_black_dragon") { npc ->
            val canMelee = CharacterTargetStrategy(npc).reached(target)
            when (random.nextInt(if (canMelee) 3 else 2)) {
                0 -> {
                    npc.anim("king_black_dragon_breath")
                    target.sound("dragon_breath")
                    nearestTile(npc, target).shoot("dragon_breath", target)
                    npc.hit(target, offensiveType = "dragonfire")
                }
                1 -> {
                    val type = specials.random()
                    npc.anim("king_black_dragon_breath")
                    target.sound("dragon_breath_$type")
                    nearestTile(npc, target).shoot("dragon_breath_$type", target)
                    npc.hit(target, offensiveType = "dragonfire", spell = type, special = true)
                }
                else -> {
                    npc.anim("king_black_dragon_attack")
                    target.sound("dragon_attack")
                    npc.hit(target, offensiveType = "melee")
                }
            }
        }

        npcCombatAttack("king_black_dragon") { npc ->
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
}
