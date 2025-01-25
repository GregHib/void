package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import content.skill.prayer.PrayerConfigs
import content.skill.ranged.combat.ammo
import content.entity.player.inv.InventoryOption
import kotlin.test.assertEquals

internal class RangedCombatFormulaTest : CombatFormulaTest() {

    @Test
    fun `Maxed player range a rat`() {
        val player = createPlayer(Skill.Ranged to 99)
        val weapon = Item("shortbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.equipment.set(EquipSlot.Ammo.index, "bronze_arrow")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "range", weapon)

        assertEquals(7920, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(127, maxHit)
        assertEquals(0.9554, chance, 0.0001)
    }

    @Test
    fun `70 range mithril arrows on cow`() {
        val player = createPlayer(Skill.Ranged to 70)
        val weapon = Item("maple_shortbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.equipment.set(EquipSlot.Ammo.index, "mithril_arrow")
        val npc = createNPC("cow")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "range", weapon)

        assertEquals(7533, offensiveRating)
        assertEquals(430, defensiveRating)
        assertEquals(113, maxHit)
        assertEquals(0.9713, chance, 0.0001)
    }

    @Test
    fun `Level 30 rapid attack style`() {
        val player = createPlayer(Skill.Ranged to 30)
        val weapon = Item("shortbow")
        player["attack_style_bow"] = 2
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.equipment.set(EquipSlot.Ammo.index, "iron_arrow")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "range", weapon)

        assertEquals(2736, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(48, maxHit)
        assertEquals(0.8710, chance, 0.0001)
    }

    @Test
    fun `Range potion boost`() {
        val player = createPlayer(Skill.Ranged to 99)
        val weapon = Item("magic_longbow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.equipment.set(EquipSlot.Ammo.index, "rune_arrow")
        val potion = Item("super_ranging_potion_4")
        player.inventory.add(potion.id)
        player.emit(InventoryOption(player, "inventory", potion, 0, "Drink"))
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "range", weapon)

        assertEquals(16359, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(222, maxHit)
        assertEquals(0.9784, chance, 0.0001)
    }

    @Test
    fun `Low prayer boost`() {
        val player = createPlayer(Skill.Ranged to 25, Skill.Prayer to 26)
        val weapon = Item("mithril_knife")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "hawk_eye")
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "range", weapon)

        assertEquals(2850, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(48, maxHit)
        assertEquals(0.8761, chance, 0.0001)
    }

    @Test
    fun `High prayer and potion boost`() {
        val player = createPlayer(Skill.Ranged to 99, Skill.Prayer to 99)
        player.addVarbit(PrayerConfigs.ACTIVE_PRAYERS, "rigour")
        val weapon = Item("dark_bow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.equipment.set(EquipSlot.Ammo.index, "dragon_arrow", 2)
        player.ammo = "dragon_arrow"
        val potion = Item("super_ranging_potion_4")
        player.inventory.add(potion.id)
        player.emit(InventoryOption(player, "inventory", potion, 0, "Drink"))
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "range", weapon)

        assertEquals(23055, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(291, maxHit)
        assertEquals(0.9847, chance, 0.0001)
    }

    @Test
    fun `Special attack boost`() {
        val player = createPlayer(Skill.Ranged to 99)
        val weapon = Item("dark_bow")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        player.equipment.set(EquipSlot.Ammo.index, "dragon_arrow")
        player.ammo = "dragon_arrow"
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "range", weapon, special = true)

        assertEquals(17490, offensiveRating)
        assertEquals(704, defensiveRating)
        assertEquals(327, maxHit)
        assertEquals(0.9798, chance, 0.0001)
    }

}