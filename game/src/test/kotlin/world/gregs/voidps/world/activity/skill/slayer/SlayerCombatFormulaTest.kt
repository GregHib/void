package world.gregs.voidps.world.activity.skill.slayer

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.player.combat.CombatFormulaTest
import kotlin.test.assertEquals

class SlayerCombatFormulaTest : CombatFormulaTest() {
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