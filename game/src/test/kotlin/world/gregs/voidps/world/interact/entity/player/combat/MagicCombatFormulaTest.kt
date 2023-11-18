package world.gregs.voidps.world.interact.entity.player.combat

import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.activity.combat.prayer.PrayerStart
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption
import kotlin.test.assertEquals

internal class MagicCombatFormulaTest : CombatFormulaTest() {

    @Test
    fun `Maxed player fire surge a rat`() {
        val player = createPlayer(Skill.Magic to 99)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", spell = "fire_wave")
        assertEquals(6912, offensiveRating)
        assertEquals(640, defensiveRating)

        assertEquals(200, maxHit)
        assertEquals(0.9536, chance, 0.0001)
    }

    @Test
    fun `83 magic ice blitz on rat`() {
        val player = createPlayer(Skill.Magic to 82)
        val npc = createNPC("cow")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", spell = "ice_blitz")
        assertEquals(5824, offensiveRating)
        assertEquals(430, defensiveRating)

        assertEquals(260, maxHit)
        assertEquals(0.9629, chance, 0.0001)
    }

    @Test
    fun `Water staff blast on rat`() {
        val player = createPlayer(Skill.Magic to 50)
        val staff = Item("mystic_steam_staff")
        player.equipment.set(EquipSlot.Weapon.index, staff.id)
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", staff, "water_blast")
        assertEquals(4366, offensiveRating)
        assertEquals(640, defensiveRating)

        assertEquals(140, maxHit)
        assertEquals(0.9265, chance, 0.0001)
    }

    @Test
    fun `Magic potion boost`() {
        val player = createPlayer(Skill.Magic to 99)
        val weapon = Item("mystic_steam_staff")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        val potion = Item("super_magic_potion_4")
        player.inventory.add(potion.id)
        player.events.emit(InventoryOption(player, "inventory", potion, 0, "Drink"))
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", weapon, "water_blast")
        assertEquals(9398, offensiveRating)
        assertEquals(640, defensiveRating)

        assertEquals(140, maxHit)
        assertEquals(0.9658, chance, 0.0001)
    }

    @Test
    fun `Low prayer boost`() {
        val player = createPlayer(Skill.Magic to 25, Skill.Prayer to 27)
        player.events.emit(PrayerStart("mystic_lore"))
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", spell = "earth_strike")
        assertEquals(2304, offensiveRating)
        assertEquals(640, defensiveRating)

        assertEquals(60, maxHit)
        assertEquals(0.8607, chance, 0.0001)
    }

    @Test
    fun `High prayer and potion boost`() {
        val player = createPlayer(Skill.Magic to 99, Skill.Prayer to 99)
        player.events.emit(PrayerStart("augury"))
        val weapon = Item("staff_of_light")
        player.equipment.set(EquipSlot.Weapon.index, weapon.id)
        val potion = Item("super_magic_potion_4")
        player.inventory.add(potion.id)
        player.events.emit(InventoryOption(player, "inventory", potion, 0, "Drink"))
        val npc = createNPC("giant_rat")

        val (offensiveRating, defensiveRating, maxHit, chance) = calculate(player, npc, "magic", weapon, "fire_wave")
        assertEquals(12636, offensiveRating)
        assertEquals(640, defensiveRating)

        assertEquals(230, maxHit)
        assertEquals(0.9746, chance, 0.0001)
    }

}