package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import kotlin.test.assertEquals

internal class SpiritShieldFormulaTest : CombatFormulaTest() {

    @Test
    fun `Divine drains prayer`() {
        val player = createPlayer(Skill.Prayer to 99)
        player.equipment.set(EquipSlot.Shield.index, "divine_spirit_shield")
        val npc = createNPC("greater_demon")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(npc, player, "melee")

        assertEquals(97, player.levels.get(Skill.Prayer))
        assertEquals(5440, offensiveRating)
        assertEquals(1251, defensiveRating)
        assertEquals(62, maxHit)
        assertEquals(0.8848, chance, 0.0001)
    }

    @Test
    fun `Elysian randomly reduces damage`() {
        val player = createPlayer()
        player.equipment.set(EquipSlot.Shield.index, "elysian_spirit_shield")
        val npc = createNPC("greater_demon")

        val (_, _, maxHit, _) = calculate(npc, player, "melee")

        val (offensiveRating, defensiveRating, _, chance) = calculate(npc, player, "melee")
        assertEquals(5440, offensiveRating)
        assertEquals(1251, defensiveRating)
        assertEquals(67, maxHit)
        assertEquals(0.8848, chance, 0.0001)
    }
}