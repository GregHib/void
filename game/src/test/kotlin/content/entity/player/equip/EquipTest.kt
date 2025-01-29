package content.entity.player.equip

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import WorldTest
import interfaceOption

internal class EquipTest : WorldTest() {

    @Test
    fun `Equip item`() {
        val player = createPlayer("player")
        player.inventory.add("bronze_sword")
        player.inventory.add("junk", 27)

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("bronze_sword"), 0)

        assertEquals(Item("bronze_sword"), player.equipped(EquipSlot.Weapon))
        assertEquals(1, player.inventory.spaces)
    }

    @Test
    fun `Can replace weapon with 2h if has one space`() {
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_sword")
        player.inventory.add("bronze_2h_sword")
        player.inventory.add("junk", 27)

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("bronze_2h_sword"), 0)

        assertEquals(Item("bronze_2h_sword"), player.equipped(EquipSlot.Weapon))
        assertTrue(player.inventory.contains("bronze_sword"))
    }

    @Test
    fun `Can replace shield with 2h if inventory is full`() {
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Shield.index, "bronze_sq_shield")
        player.inventory.add("bronze_2h_sword")
        player.inventory.add("junk", 27)

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("bronze_2h_sword"), 0)

        assertEquals(Item("bronze_2h_sword"), player.equipped(EquipSlot.Weapon))
        assertTrue(player.equipped(EquipSlot.Shield).isEmpty())
        assertTrue(player.inventory.contains("bronze_sq_shield"))
    }

    @Test
    fun `Can replace shield and weapon with 2h if has one space`() {
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_sword")
        player.equipment.set(EquipSlot.Shield.index, "bronze_sq_shield")
        player.inventory.add("bronze_2h_sword")
        player.inventory.add("junk", 26)

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("bronze_2h_sword"), 0)

        assertEquals(Item("bronze_2h_sword"), player.equipped(EquipSlot.Weapon))
        assertTrue(player.equipped(EquipSlot.Shield).isEmpty())
        assertTrue(player.inventory.contains("bronze_sword"))
        assertTrue(player.inventory.contains("bronze_sq_shield"))
    }

    @Test
    fun `Can't replace shield and weapon with 2h if inventory is full`() {
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_sword")
        player.equipment.set(EquipSlot.Shield.index, "bronze_sq_shield")
        player.inventory.add("bronze_2h_sword")
        player.inventory.add("junk", 27)

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("bronze_2h_sword"), 0)

        assertEquals(Item("bronze_sword"), player.equipped(EquipSlot.Weapon))
        assertEquals(Item("bronze_sq_shield"), player.equipped(EquipSlot.Shield))
        assertTrue(player.inventory.contains("bronze_2h_sword"))
    }

    @Test
    fun `Can replace 2h with weapon when inventory is full`() {
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_2h_sword")
        player.inventory.add("bronze_sword")
        player.inventory.add("junk", 27)

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("bronze_sword"), 0)

        assertEquals(Item("bronze_sword"), player.equipped(EquipSlot.Weapon))
        assertTrue(player.inventory.contains("bronze_2h_sword"))
    }

    @Test
    fun `Can replace 2h with shield when inventory is full`() {
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_2h_sword")
        player.inventory.add("bronze_sq_shield")
        player.inventory.add("junk", 27)

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("bronze_sq_shield"), 0)

        assertTrue(player.equipped(EquipSlot.Weapon).isEmpty())
        assertEquals(Item("bronze_sq_shield"), player.equipped(EquipSlot.Shield))
        assertTrue(player.inventory.contains("bronze_2h_sword"))
    }

    @Test
    fun `Remove equipped item`() {
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_sword")

        player.interfaceOption("worn_equipment", "weapon_slot", "*", 0, Item("bronze_sword"))

        assertTrue(player.inventory.contains("bronze_sword"))
        assertTrue(player.equipment.isEmpty())
    }

    @Test
    fun `Stack equipped items`() {
        val player = createPlayer("player")
        player.experience.add(Skill.Ranged, Experience.MAXIMUM_EXPERIENCE)
        player.levels.set(Skill.Ranged, 99)
        player.equipment.set(EquipSlot.Ammo.index, "rune_arrow", 10)
        player.inventory.add("rune_arrow", 40)

        player.interfaceOption("inventory", "inventory", "Wield", 1, Item("rune_arrow"), 0)

        assertEquals(Item("rune_arrow", 50), player.equipped(EquipSlot.Ammo))
        assertTrue(player.inventory.isEmpty())
    }
}