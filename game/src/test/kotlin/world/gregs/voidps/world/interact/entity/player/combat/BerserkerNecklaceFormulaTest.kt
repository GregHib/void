package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.visual.update.player.EquipSlot
import kotlin.test.assertEquals

internal class BerserkerNecklaceFormulaTest : CombatFormulaTest() {

    @Test
    fun `No berserker effect with non-tzhaar weapon`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.set(EquipSlot.Amulet.index, "berserker_necklace")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_sword")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("bronze_sword"))

        assertEquals(5848, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(132, maxHit)
        assertEquals(0.9396, chance, 0.0001)
    }

    @Test
    fun `Toktz-xil-ak berserker weapon effect`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.set(EquipSlot.Amulet.index, "berserker_necklace")
        player.equipment.set(EquipSlot.Weapon.index, "toktz_xil_ak")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("toktz_xil_ak"))

        assertEquals(9546, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(246, maxHit)
        assertEquals(0.963, chance, 0.0001)
    }

    @Test
    fun `Tzhaar-ket-om berserker weapon effect`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.set(EquipSlot.Amulet.index, "berserker_necklace")
        player.equipment.set(EquipSlot.Weapon.index, "tzhaar_ket_om")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("tzhaar_ket_om"))

        assertEquals(12384, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(318, maxHit)
        assertEquals(0.9714, chance, 0.0001)
    }

    @Test
    fun `Tzhaar-ket-em berserker weapon effect`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.set(EquipSlot.Amulet.index, "berserker_necklace")
        player.equipment.set(EquipSlot.Weapon.index, "tzhaar_ket_em")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("tzhaar_ket_em"))

        assertEquals(10836, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(260, maxHit)
        assertEquals(0.9674, chance, 0.0001)
    }

    @Test
    fun `Toktz-xil-ek berserker weapon effect`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.set(EquipSlot.Amulet.index, "berserker_necklace")
        player.equipment.set(EquipSlot.Weapon.index, "toktz_xil_ek")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", Item("toktz_xil_ek"))

        assertEquals(9632, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(225, maxHit)
        assertEquals(0.9633, chance, 0.0001)
    }
}