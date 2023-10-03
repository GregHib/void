package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.PlayerLevels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.activity.combat.prayer.PrayerStart
import world.gregs.voidps.world.interact.entity.combat.combatStyle
import world.gregs.voidps.world.interact.entity.combat.getMaximumHit
import world.gregs.voidps.world.interact.entity.combat.getRating
import world.gregs.voidps.world.interact.entity.combat.hitChance
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption
import world.gregs.voidps.world.script.WorldTest
import kotlin.test.assertEquals

abstract class CombatFormulaTest : WorldTest() {

    internal data class Results(
        val offensiveRating: Int,
        val defensiveRating: Int,
        val maxHit: Int,
        val chance: Double
    )

    internal fun calculate(player: Player, target: Character, type: String, weapon: Item? = null, spell: String = "", special: Boolean = false): Results {
        val offensiveRating = getRating(player, player, type, weapon, special)
        val defensiveRating = getRating(player, target, type, weapon, special)
        val maxHit = getMaximumHit(player, target, type, weapon, spell, special)
        val chance = hitChance(player, target, type, weapon, special)
        return Results(offensiveRating, defensiveRating, maxHit, chance)
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