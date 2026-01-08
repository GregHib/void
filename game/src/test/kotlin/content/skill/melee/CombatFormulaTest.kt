package content.skill.melee

import WorldTest
import content.entity.combat.hit.Damage
import content.entity.combat.hit.Hit
import world.gregs.voidps.engine.data.definition.CombatDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.get

abstract class CombatFormulaTest : WorldTest() {

    internal data class Results(
        val offensiveRating: Int,
        val defensiveRating: Int,
        val maxHit: Int,
        val chance: Double,
    )

    internal fun calculate(source: Player, target: Character, type: String, weapon: Item = Item.EMPTY, spell: String = "", special: Boolean = false): Results {
        val offensiveRating = Hit.rating(source, target, type, weapon, special, true)
        val defensiveRating = Hit.rating(source, target, type, weapon, special, false)
        val maxHit = Damage.maximum(source, target, type, weapon, spell, special)
        val actualMaxHit = Damage.modify(source, target, type, maxHit, weapon, spell, special)
        val chance = Hit.chance(source, target, type, weapon, special)
        return Results(offensiveRating, defensiveRating, actualMaxHit, chance)
    }

    internal fun calculate(source: NPC, target: Character, attack: String, weapon: Item = Item.EMPTY, special: Boolean = false): Results {
        val attackDef = get<CombatDefinitions>().getOrNull(source.id)?.attacks?.get(attack)
        val hit = attackDef?.targetHits?.firstOrNull()
        if (hit != null) {
            val range = hit.min..hit.max
            val type = hit.offense
            val maxHit = Damage.maximum(source, target, type, weapon, "", special, range)
            val offensiveRating = Hit.rating(source, target, type, weapon, special, true)
            val defensiveRating = Hit.rating(source, target, type, weapon, special, false)
            val actualMaxHit = Damage.modify(source, target, type, maxHit, weapon, "", special)
            val chance = Hit.chance(source, target, type, weapon, special)
            return Results(offensiveRating, defensiveRating, actualMaxHit, chance)
        } else {
            val maxHit = Damage.maximum(source, target, attack, weapon, "")
            val offensiveRating = Hit.rating(source, target, attack, weapon, special, true)
            val defensiveRating = Hit.rating(source, target, attack, weapon, special, false)
            val actualMaxHit = Damage.modify(source, target, attack, maxHit, weapon, "", special)
            val chance = Hit.chance(source, target, attack, weapon, special)
            return Results(offensiveRating, defensiveRating, actualMaxHit, chance)
        }
    }

    internal fun createPlayer(vararg pairs: Pair<Skill, Int>): Player {
        val player = createPlayer()
        for ((skill, level) in pairs) {
            player.levels.set(skill, level)
            player.experience.set(skill, Level.experience(skill, level))
        }
        return player
    }
}
