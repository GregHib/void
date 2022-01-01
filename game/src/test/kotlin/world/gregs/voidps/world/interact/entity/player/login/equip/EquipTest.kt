package world.gregs.voidps.world.interact.entity.player.login.equip

import io.mockk.every
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.engine.entity.character.contain.equipment
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.world.script.WorldMock
import world.gregs.voidps.world.script.interfaceOption

internal class EquipTest : WorldMock() {

    @Test
    fun `Equip item`() = runBlocking(Dispatchers.Default) {
        every { get<ItemDecoder>().get(1277) } returns ItemDefinition( // bronze_sword
            id = 1277,
            options = arrayOf("Wield", null, null, null, "Drop")
        )
        val player = createPlayer("player")
        player.inventory.add("bronze_sword")
        player.inventory.add("junk", 27)

        player.interfaceOption("inventory", "container", "Wield", 0, Item("bronze_sword"), 0)

        assertEquals(Item("bronze_sword", 1), player.equipped(EquipSlot.Weapon))
        assertEquals(1, player.inventory.spaces)
    }

    @Test
    fun `Can replace weapon with 2h if has one space`() {
        every { get<ItemDecoder>().get(1307) } returns ItemDefinition( // bronze_2h_sword
            id = 1307,
            options = arrayOf("Wield", null, null, null, "Drop")
        )
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_sword")
        player.inventory.add("bronze_2h_sword")
        player.inventory.add("junk", 27)

        player.interfaceOption("inventory", "container", "Wield", 0, Item("bronze_2h_sword"), 0)

        assertEquals(Item("bronze_2h_sword", 1), player.equipped(EquipSlot.Weapon))
        assertTrue(player.inventory.contains("bronze_sword"))
    }

    @Test
    fun `Can replace shield with 2h if inventory is full`() {
        every { get<ItemDecoder>().get(1307) } returns ItemDefinition( // bronze_2h_sword
            id = 1307,
            options = arrayOf("Wield", null, null, null, "Drop")
        )
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Shield.index, "bronze_square_shield")
        player.inventory.add("bronze_2h_sword")
        player.inventory.add("junk", 27)

        player.interfaceOption("inventory", "container", "Wield", 0, Item("bronze_2h_sword"), 0)

        assertEquals(Item("bronze_2h_sword", 1), player.equipped(EquipSlot.Weapon))
        assertTrue(player.equipped(EquipSlot.Shield).isEmpty())
        assertTrue(player.inventory.contains("bronze_square_shield"))
    }

    @Test
    fun `Can replace shield and weapon with 2h if has one space`() {
        every { get<ItemDecoder>().get(1307) } returns ItemDefinition( // bronze_2h_sword
            id = 1307,
            options = arrayOf("Wield", null, null, null, "Drop")
        )
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_sword")
        player.equipment.set(EquipSlot.Shield.index, "bronze_square_shield")
        player.inventory.add("bronze_2h_sword")
        player.inventory.add("junk", 26)

        player.interfaceOption("inventory", "container", "Wield", 0, Item("bronze_2h_sword"), 0)

        assertEquals(Item("bronze_2h_sword", 1), player.equipped(EquipSlot.Weapon))
        assertTrue(player.equipped(EquipSlot.Shield).isEmpty())
        assertTrue(player.inventory.contains("bronze_sword"))
        assertTrue(player.inventory.contains("bronze_square_shield"))
    }

    @Test
    fun `Can't replace shield and weapon with 2h if inventory is full`() {
        every { get<ItemDecoder>().get(1307) } returns ItemDefinition( // bronze_2h_sword
            id = 1307,
            options = arrayOf("Wield", null, null, null, "Drop")
        )
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_sword")
        player.equipment.set(EquipSlot.Shield.index, "bronze_square_shield")
        player.inventory.add("bronze_2h_sword")
        player.inventory.add("junk", 27)

        player.interfaceOption("inventory", "container", "Wield", 0, Item("bronze_2h_sword"), 0)

        assertEquals(Item("bronze_sword", 1), player.equipped(EquipSlot.Weapon))
        assertEquals(Item("bronze_square_shield", 1), player.equipped(EquipSlot.Shield))
        assertTrue(player.inventory.contains("bronze_2h_sword"))
    }

    @Test
    fun `Can replace 2h with weapon when inventory is full`() {
        every { get<ItemDecoder>().get(1277) } returns ItemDefinition( // bronze_sword
            id = 1277,
            options = arrayOf("Wield", null, null, null, "Drop")
        )
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_2h_sword")
        player.inventory.add("bronze_sword")
        player.inventory.add("junk", 27)

        player.interfaceOption("inventory", "container", "Wield", 0, Item("bronze_sword"), 0)

        assertEquals(Item("bronze_sword", 1), player.equipped(EquipSlot.Weapon))
        assertTrue(player.inventory.contains("bronze_2h_sword"))
    }

    @Test
    fun `Can replace 2h with shield when inventory is full`() {
        every { get<ItemDecoder>().get(1173) } returns ItemDefinition( // bronze_square_shield
            id = 1173,
            options = arrayOf("Wield", null, null, null, "Drop")
        )
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_2h_sword")
        player.inventory.add("bronze_square_shield")
        player.inventory.add("junk", 27)

        player.interfaceOption("inventory", "container", "Wield", 0, Item("bronze_square_shield"), 0)

        println(player.inventory.getItems().toList())
        println(player.equipment.getItems().toList())

        assertTrue(player.equipped(EquipSlot.Weapon).isEmpty())
        assertEquals(Item("bronze_square_shield", 1), player.equipped(EquipSlot.Shield))
        assertTrue(player.inventory.contains("bronze_2h_sword"))
    }

    @Test
    fun `Remove equipped item`() = runBlocking(Dispatchers.Default) {
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Weapon.index, "bronze_sword")

        player.interfaceOption("worn_equipment", "weapon", "*", 0, Item("bronze_sword"))

        assertTrue(player.inventory.contains("bronze_sword"))
        assertTrue(player.equipment.isEmpty())
    }

    @Test
    fun `Stack equipped items`() = runBlocking(Dispatchers.Default) {
        every { get<ItemDecoder>().get(892) } returns ItemDefinition( // rune_arrow
            id = 892,
            stackable = 1,
            options = arrayOf("Wield", null, null, null, "Drop")
        )
        val player = createPlayer("player")
        player.equipment.set(EquipSlot.Ammo.index, "rune_arrow", 10)
        player.inventory.add("rune_arrow", 40)

        player.interfaceOption("inventory", "container", "Wield", 0, Item("rune_arrow"), 0)

        assertEquals(Item("rune_arrow", 50), player.equipped(EquipSlot.Ammo))
        assertTrue(player.inventory.isEmpty())
    }
}