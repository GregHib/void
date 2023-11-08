package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.network.visual.update.player.EquipSlot
import kotlin.test.assertEquals

internal class VoidSetEffectFormulaTest : CombatFormulaTest() {

    @Test
    fun `No void set effect with one missing item`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.apply(void("melee"))
        player.equipment.clear(EquipSlot.Hat.index)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")

        assertEquals(5504, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(112, maxHit)
        assertEquals(0.9359, chance, 0.0001)
    }

    @Test
    fun `Melee void set effect`() {
        val player = createPlayer(Skill.Attack to 75, Skill.Strength to 99)
        player.equipment.apply(void("melee"))
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "melee")

        assertEquals(6016, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(122, maxHit)
        assertEquals(0.9413, chance, 0.0001)
    }

    @Test
    fun `Ranged void set effect`() {
        val player = createPlayer(Skill.Ranged to 75)
        player.equipment.apply(void("ranger"))
        player.equipment.set(EquipSlot.Weapon.index, "shortbow")
        player.equipment.set(EquipSlot.Ammo.index, "bronze_arrow")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "range", Item("shortbow"))

        assertEquals(6768, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(109, maxHit)
        assertEquals(0.9479, chance, 0.0001)
    }

    @Test
    fun `Magic void set effect`() {
        val player = createPlayer(Skill.Magic to 75)
        player.equipment.apply(void("mage"))
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", spell = "wind_strike")

        assertEquals(6848, offensiveRating)
        assertEquals(64, defensiveRating)
        assertEquals(20, maxHit)
        assertEquals(0.9951, chance, 0.0001)
    }

    private fun void(helmType: String): Inventory.() -> Unit = {
        set(EquipSlot.Chest.index, "void_knight_top")
        set(EquipSlot.Legs.index, "void_knight_robe")
        set(EquipSlot.Hands.index, "void_knight_gloves")
        set(EquipSlot.Hat.index, "void_${helmType}_helm")
    }
}