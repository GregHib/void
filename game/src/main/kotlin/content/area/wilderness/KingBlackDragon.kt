package content.area.wilderness

import content.entity.combat.hit.hit
import content.entity.effect.freeze
import content.entity.effect.toxin.poison
import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.move.target.CharacterTargetStrategy
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class KingBlackDragon : Script {

    val specials = listOf("toxic", "ice", "shock")

    init {
        npcCombatSwing("king_black_dragon") { target ->
            val canMelee = CharacterTargetStrategy(this).reached(target)
            when (random.nextInt(if (canMelee) 3 else 2)) {
                0 -> {
                    anim("king_black_dragon_breath")
                    target.sound("dragon_breath")
                    nearestTile(this, target).shoot("dragon_breath", target)
                    hit(target, offensiveType = "dragonfire")
                }
                1 -> {
                    val type = specials.random()
                    anim("king_black_dragon_breath")
                    target.sound("dragon_breath_$type")
                    nearestTile(this, target).shoot("dragon_breath_$type", target)
                    hit(target, offensiveType = "dragonfire", spell = type, special = true)
                }
                else -> {
                    anim("king_black_dragon_attack")
                    target.sound("dragon_attack")
                    hit(target, offensiveType = "melee")
                }
            }
        }

        npcCombatAttack("king_black_dragon") { (target, _, _, _, spell) ->
            when (spell) {
                "toxic" -> poison(target, 80)
                "ice" -> freeze(target, 10)
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
