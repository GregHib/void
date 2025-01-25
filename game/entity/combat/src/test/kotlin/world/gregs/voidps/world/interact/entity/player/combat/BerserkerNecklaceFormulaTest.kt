package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import kotlin.test.assertEquals

internal class BerserkerNecklaceFormulaTest : CombatFormulaTest() {

    @Test
    fun `No berserker effect with non-tzhaar weapon`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        val weapon = Item("bronze_sword")
        player.equipment.set(EquipSlot.Amulet.index, "berserker_necklace")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", weapon)

        assertEquals(5848, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(132, maxHit)
        assertEquals(0.9396, chance, 0.0001)
    }

    @Test
    fun `Toktz-xil-ak berserker weapon effect`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        val weapon = Item("toktz_xil_ak")
        player.equipment.set(EquipSlot.Amulet.index, "berserker_necklace")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", weapon)

        assertEquals(9546, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(246, maxHit)
        assertEquals(0.963, chance, 0.0001)
    }

    @Test
    fun `Tzhaar-ket-om berserker weapon effect`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        val weapon = Item("tzhaar_ket_om")
        player.equipment.set(EquipSlot.Amulet.index, "berserker_necklace")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", weapon)

        assertEquals(12384, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(318, maxHit)
        assertEquals(0.9714, chance, 0.0001)
    }

    @Test
    fun `Tzhaar-ket-em berserker weapon effect`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        val weapon = Item("tzhaar_ket_em")
        player.equipment.set(EquipSlot.Amulet.index, "berserker_necklace")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", weapon)

        assertEquals(10836, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(260, maxHit)
        assertEquals(0.9674, chance, 0.0001)
    }

    @Test
    fun `Toktz-xil-ek berserker weapon effect`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        val weapon = Item("toktz_xil_ek")
        player.equipment.set(EquipSlot.Amulet.index, "berserker_necklace")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee", weapon)

        assertEquals(9632, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(225, maxHit)
        assertEquals(0.9633, chance, 0.0001)
    }
}