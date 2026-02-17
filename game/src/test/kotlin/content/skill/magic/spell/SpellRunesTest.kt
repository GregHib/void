package content.skill.magic.spell

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import set
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class SpellRunesTest : MagicSpellTest() {

    @Test
    fun `Spell with elemental runes`() {
        val player = player()
        player.levels.set(Skill.Magic, 10)
        setLevel(10)
        setItems(Item("air_rune"))
        assertFalse(player.hasSpellItems("spell", message = false))

        player.inventory.add("air_rune")

        assertTrue(player.removeSpellItems("spell"))
        assertTrue(player.inventory.isEmpty())
    }

    @Test
    fun `Invalid spell and requirements`() {
        val player = player()
        assertFalse(player.hasSpellItems("invalid", message = false))
        assertFalse(player.removeSpellItems("invalid"))

        InterfaceDefinitions.definitions.first().components!![0]!!.information = null

        assertFalse(player.hasSpellItems("spell", message = false))
        assertFalse(player.removeSpellItems("spell"))
    }

    @Test
    fun `Members spell`() {
        Settings.load(mapOf("world.members" to "false"))

        val player = player()
        setItems(Item("blood_rune") to ItemDefinition(stringId = "blood_rune", members = true))
        player.inventory.add("blood_rune", 10)

        assertFalse(player.hasSpellItems("spell", message = false))
        assertFalse(player.removeSpellItems("spell"))
        Settings.clear()
    }

    @Test
    fun `Doesn't have magic level`() {
        val player = player()
        player.levels.set(Skill.Magic, 5)
        setLevel(10)
        setItems(Item("air_rune"))
        player.inventory.add("air_rune")

        assertFalse(player.hasSpellItems("spell", message = false))
        assertFalse(player.removeSpellItems("spell"))
        assertTrue(player.inventory.contains("air_rune"))
    }

    @Test
    fun `Spell with multiple elemental runes`() {
        val player = player()
        setItems(Item("air_rune", 3))

        player.inventory.add("air_rune")
        assertFalse(player.hasSpellItems("spell"))
        player.inventory.add("air_rune", 2)
        assertTrue(player.removeSpellItems("spell"))
        assertTrue(player.inventory.isEmpty())
    }

    @Test
    fun `Spell with mix of runes`() {
        val player = player()
        setItems(Item("air_rune"), Item("fire_rune"), Item("mind_rune"))

        player.inventory.add("air_rune")
        player.inventory.add("fire_rune")
        assertFalse(player.hasSpellItems("spell", message = false))
        player.inventory.add("mind_rune")

        assertTrue(player.removeSpellItems("spell"))
        assertTrue(player.inventory.isEmpty())
    }

    @Test
    fun `Spell with required staff`() {
        val player = player()
        setItems(Item("air_rune"), Item("slayers_staff"))

        player.inventory.add("air_rune")
        assertFalse(player.hasSpellItems("spell", message = false))
        player.equipment.set(EquipSlot.Weapon.index, "slayers_staff")

        assertTrue(player.removeSpellItems("spell"))
        assertTrue(player.inventory.isEmpty())
        assertEquals("slayers_staff", player.equipped(EquipSlot.Weapon).id)
    }

    @Test
    fun `Spell with required staff not equipped`() {
        val player = player()
        setItems(Item("air_rune"), Item("slayers_staff"))

        player.inventory.add("air_rune")
        player.inventory.add("slayers_staff")
        assertFalse(player.hasSpellItems("spell", message = false))
        assertFalse(player.removeSpellItems("spell"))
        assertTrue(player.inventory.contains("air_rune"))
        assertTrue(player.inventory.contains("slayers_staff"))
    }

    @Test
    fun `Staff with infinite runes`() {
        val player = player()
        setItems(Item("air_rune"), Item("chaos_rune"))
        addItemDef(ItemDefinition(stringId = "staff_of_air", extras = mapOf("infinite_air_runes" to 1)))

        player.inventory.add("air_rune", 10)
        player.inventory.add("chaos_rune", 10)
        player.equipment.set(EquipSlot.Weapon.index, "staff_of_air")

        assertTrue(player.removeSpellItems("spell"))
        assertEquals(10, player.inventory.count("air_rune"))
        assertEquals(9, player.inventory.count("chaos_rune"))
        assertEquals("staff_of_air", player.equipped(EquipSlot.Weapon).id)
    }

    @ParameterizedTest
    @ValueSource(strings = ["guthix_staff", "void_knight_mace", "slayers_staff", "staff_of_light_red", "zuriels_staff_corrupted", "ibans_staff", "saradomin_staff", "zamorak_staff"])
    fun `Has required staff`(staff: String) {
        val player = player()
        val required = when (staff) {
            "guthix_staff", "void_knight_mace" -> "guthix_staff_dummy"
            "staff_of_light_red" -> "slayers_staff"
            "zuriels_staff_corrupted" -> "zuriels_staff"
            else -> staff
        }
        setItems(Item(required), Item("air_rune", 2))

        player.inventory.add("air_rune", 10)
        player.equipment.set(EquipSlot.Weapon.index, staff)

        assertTrue(player.removeSpellItems("spell"))
        assertEquals(8, player.inventory.count("air_rune"))
        assertEquals(staff, player.equipped(EquipSlot.Weapon).id)
    }
}
