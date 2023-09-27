package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.combatStyle
import world.gregs.voidps.world.interact.entity.combat.getMaximumHit
import world.gregs.voidps.world.interact.entity.combat.getRating
import world.gregs.voidps.world.interact.entity.combat.hitChance
import world.gregs.voidps.world.script.WorldTest
import kotlin.test.assertEquals

internal class MeleeCombatFormulaTest : WorldTest() {

    private data class Results(
        val offensiveRating: Int,
        val defensiveRating: Int,
        val maxHit: Int,
        val chance: Double
    )

    private fun calculate(player: Player, target: Character, type: String, weapon: Item? = null, spell: String = "", special: Boolean = false): Results {
        val offensiveRating = getRating(player, player, type, weapon, special)
        val defensiveRating = getRating(player, target, type, weapon, special)
        val maxHit = getMaximumHit(player, target, type, weapon, spell, special)
        val chance = hitChance(player, target, type, weapon, special)
        return Results(offensiveRating, defensiveRating, maxHit, chance)
    }

    @Test
    fun `Maxed player punching a rat`() {
        val player = createPlayer("player")
        for (skill in Skill.all) {
            player.levels.set(skill, 99)
        }
        val npc = createNPC("rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")
        assertEquals(7040, offensiveRating)
        assertEquals(220, defensiveRating)

        assertEquals(112, maxHit)
        assertEquals(0.9842, chance, 0.0001)
    }

    @Test
    fun `80 strength player punching a cow`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Strength, 80)
        val npc = createNPC("cow")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")

        assertEquals(768, offensiveRating)
        assertEquals(430, defensiveRating)

        assertEquals(93, maxHit)
        assertEquals(0.7191, chance, 0.0001)
    }

    @Test
    fun `Level 13 bronze scimitar on a giant rat`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Attack, 20)
        player.levels.set(Skill.Strength, 20)
        player.equipment.set(EquipSlot.Weapon.index, "bronze_scimitar")
        val npc = createNPC("giant_rat")
        println(player.combatStyle)

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("bronze_scimitar"))

        assertEquals(2201, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(35, maxHit)
        assertEquals(0.8397, chance, 0.0001)
    }

    @Test
    fun `Level 37 crush attack style`() {
        val player = createPlayer("player")
        player.levels.set(Skill.Attack, 55)
        player.levels.set(Skill.Strength, 60)
        player["attack_style_axe"] = 2
        player.equipment.set(EquipSlot.Weapon.index, "rune_battleaxe")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("bronze_scimitar"))

        assertEquals(6741, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(147, maxHit)
        assertEquals(0.9476, chance, 0.0001)
    }
}