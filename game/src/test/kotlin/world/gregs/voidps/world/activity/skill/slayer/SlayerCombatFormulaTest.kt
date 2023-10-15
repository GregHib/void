package world.gregs.voidps.world.activity.skill.slayer

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.player.combat.CombatFormulaTest
import kotlin.test.assertEquals

class SlayerCombatFormulaTest : CombatFormulaTest() {

    @Test
    fun `Salve amulet boost melee on undead target`() {
        val player = createPlayer(Skill.Attack to 50, Skill.Strength to 50)
        player.equipment.set(EquipSlot.Amulet.index, "salve_amulet")
        val npc = createNPC("zombie")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")

        assertEquals(4554, offensiveRating)
        assertEquals(1216, defensiveRating)
        assertEquals(73, maxHit)
        assertEquals(0.8663, chance, 0.0001)
    }

    @Test
    fun `Salve amulet no boost melee on regular target`() {
        val player = createPlayer(Skill.Attack to 50, Skill.Strength to 50)
        player.equipment.set(EquipSlot.Amulet.index, "salve_amulet_e")
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")

        assertEquals(3904, offensiveRating)
        assertEquals(5760, defensiveRating)
        assertEquals(63, maxHit)
        assertEquals(0.3388, chance, 0.0001)
    }

    @Test
    fun `Salve amulet no boost with magic or ranged`() {
        val player = createPlayer(Skill.Magic to 50)
        player.equipment.set(EquipSlot.Amulet.index, "salve_amulet")
        val npc = createNPC("zombie")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", spell = "wind_strike")

        assertEquals(4342, offensiveRating)
        assertEquals(64, defensiveRating)
        assertEquals(20, maxHit)
        assertEquals(0.9261, chance, 0.0001)
    }

    @Test
    fun `Salve amulet e overrides slayer helmet boost`() {

    }


    @Test
    fun `Slayer helmet not on task`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.set(EquipSlot.Hat.index, "slayer_helmet")
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")

        assertEquals(5504, offensiveRating)
        assertEquals(5760, defensiveRating)
        assertEquals(112, maxHit)
        assertEquals(0.4777, chance, 0.0001)
    }

    @Test
    fun `Slayer helmet on slayer task`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.set(EquipSlot.Hat.index, "slayer_helmet")
        player["slayer_task"] = true
        player["slayer_type"] = "demon"
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")

        assertEquals(5504, offensiveRating)
        assertEquals(5760, defensiveRating)
        assertEquals(128, maxHit)
        assertEquals(0.4777, chance, 0.0001)
    }
}