package world.gregs.voidps.world.interact.entity.player.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.world.interact.entity.combat.Weapon
import world.gregs.voidps.world.interact.entity.combat.hit.Damage
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.script.WorldTest

abstract class CombatFormulaTest : WorldTest() {

    internal data class Results(
        val offensiveRating: Int,
        val defensiveRating: Int,
        val maxHit: Int,
        val chance: Double
    )

    internal fun calculate(source: Character, target: Character, type: String, weapon: Item = Item.EMPTY, spell: String = "", special: Boolean = false): Results {
        val offensiveRating = Hit.rating(source, target, type, weapon, special, true)
        val defensiveRating = Hit.rating(source, target, type, weapon, special, false)
        val maxHit = Damage.maximum(source, type, weapon, spell)
        val strengthBonus = Weapon.strengthBonus(source, type, weapon)
        val actualMaxHit = Damage.modify(source, target, type, strengthBonus, maxHit, weapon, spell, special)
        val chance = Hit.chance(source, target, type, weapon, special)
        return Results(offensiveRating, defensiveRating, actualMaxHit, chance)
    }

    internal fun createPlayer(vararg pairs: Pair<Skill, Int>): Player {
        val player = createPlayer("player")
        for ((skill, level) in pairs) {
            player.levels.set(skill, level)
            player.experience.set(skill, PlayerLevels.getExperience(level, skill))
        }
        return player
    }

}