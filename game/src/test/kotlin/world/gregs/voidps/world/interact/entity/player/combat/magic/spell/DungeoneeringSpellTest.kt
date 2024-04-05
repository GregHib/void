package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.FakeRandom
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom
import world.gregs.voidps.world.interact.entity.player.effect.degrade.Degrade
import world.gregs.voidps.world.script.set

class DungeoneeringSpellTest : MagicSpellTest() {

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextInt(from: Int, until: Int) = 1
        })
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Remove blast box charges`(bolt: Boolean) {
        val player = player()
        val catalyst = if (bolt) "chaos_rune" else "death_rune"
        setItems(Item("fire_rune", 1, def = ItemDefinition.EMPTY), Item("air_rune", 1, def = ItemDefinition.EMPTY), Item(catalyst, 1, def = ItemDefinition.EMPTY))
        addItemDef(ItemDefinition(stringId = "magical_blastbox_bound", extras = mapOf("charges" to 1234, "charge_start" to 10)))

        player.inventory.add("air_rune", 10)
        player.inventory.add("fire_rune", 10)
        player.inventory.add(catalyst, 10)
        player.equipment.set(EquipSlot.Shield.index, "magical_blastbox_bound")

        assertTrue(player.removeSpellItems("spell_${if (bolt) "bolt" else "blast"}"))
        assertEquals(10, player.inventory.count("air_rune"))
        assertEquals(9, player.inventory.count("fire_rune"))
        assertEquals(10, player.inventory.count(catalyst))
        assertEquals("magical_blastbox_bound", player.equipped(EquipSlot.Shield).id)
        assertEquals(9, Degrade.charges(player, player.equipment, EquipSlot.Shield.index))
    }

    @Test
    fun `Remove surge box wave charges`() {
        val player = player()
        setItems(Item("earth_rune", 4, def = ItemDefinition.EMPTY), Item("air_rune", 1, def = ItemDefinition.EMPTY), Item("blood_rune", 1, def = ItemDefinition.EMPTY))
        addItemDef(ItemDefinition(stringId = "celestial_surgebox", extras = mapOf("charges" to 1234, "charge_start" to 10)))

        player.inventory.add("air_rune", 10)
        player.inventory.add("earth_rune", 10)
        player.inventory.add("blood_rune", 10)
        player.equipment.set(EquipSlot.Shield.index, "celestial_surgebox")

        assertTrue(player.removeSpellItems("spell_wave"))
        assertEquals(10, player.inventory.count("air_rune"))
        assertEquals(6, player.inventory.count("earth_rune"))
        assertEquals(10, player.inventory.count("blood_rune"))
        assertEquals("celestial_surgebox", player.equipped(EquipSlot.Shield).id)
        assertEquals(9, Degrade.charges(player, player.equipment, EquipSlot.Shield.index))
    }

    @Test
    fun `Remove surge box surge charges`() {
        val player = player()
        setItems(Item("earth_rune", 4, def = ItemDefinition.EMPTY),
            Item("air_rune", 1, def = ItemDefinition.EMPTY),
            Item("death_rune", 1, def = ItemDefinition.EMPTY),
            Item("blood_rune", 1, def = ItemDefinition.EMPTY))
        addItemDef(ItemDefinition(stringId = "celestial_surgebox", extras = mapOf("charges" to 1234, "charge_start" to 10)))

        player.inventory.add("air_rune", 10)
        player.inventory.add("earth_rune", 10)
        player.inventory.add("blood_rune", 10)
        player.inventory.add("death_rune", 10)
        player.equipment.set(EquipSlot.Shield.index, "celestial_surgebox")

        assertTrue(player.removeSpellItems("spell_surge"))
        assertEquals(10, player.inventory.count("air_rune"))
        assertEquals(6, player.inventory.count("earth_rune"))
        assertEquals(10, player.inventory.count("blood_rune"))
        assertEquals(10, player.inventory.count("death_rune"))
        assertEquals("celestial_surgebox", player.equipped(EquipSlot.Shield).id)
        assertEquals(9, Degrade.charges(player, player.equipment, EquipSlot.Shield.index))
    }

    @ParameterizedTest
    @ValueSource(strings = [ "magic_blastbox", "celestial_surgebox"])
    fun `Dungeoneering box charges don't count towards other spells`(box: String) {
        val player = player()
        setItems(Item("air_rune", 1, def = ItemDefinition.EMPTY), Item("chaos_rune", 1, def = ItemDefinition.EMPTY))
        addItemDef(ItemDefinition(stringId = box, extras = mapOf("charges" to 1234, "charge_start" to 10)))

        player.inventory.add("air_rune", 10)
        player.inventory.add("chaos_rune", 10)
        player.equipment.set(EquipSlot.Shield.index, box)

        assertTrue(player.removeSpellItems("spell"))
        assertEquals(9, player.inventory.count("air_rune"))
        assertEquals(9, player.inventory.count("chaos_rune"))
        assertEquals(box, player.equipped(EquipSlot.Shield).id)
        assertEquals(10, Degrade.charges(player, player.equipment, EquipSlot.Shield.index))
    }

    @ParameterizedTest
    @ValueSource(strings = [ "spell_bolt", "spell_blast", "spell_wave", "spell_surge"])
    fun `Can't cast without dungeoneering box charges`(spell: String) {
        val player = player()
        val box = if (spell.endsWith("bolt") || spell.endsWith("blast")) "magic_blastbox" else "celestial_surgebox"
        setItems(Item("air_rune", 1, def = ItemDefinition.EMPTY), Item("chaos_rune", 1, def = ItemDefinition.EMPTY))
        addItemDef(ItemDefinition(stringId = box, extras = mapOf("charges" to 1234, "charge_start" to 0)))

        player.equipment.set(EquipSlot.Shield.index, box)

        assertFalse(player.removeSpellItems(spell))
        assertEquals(box, player.equipped(EquipSlot.Shield).id)
        assertEquals(0, Degrade.charges(player, player.equipment, EquipSlot.Shield.index))
    }

    @ParameterizedTest
    @ValueSource(strings = [ "spell_bolt", "spell_blast", "spell_wave", "spell_surge"])
    fun `Can cast only with dungeoneering box charges`(spell: String) {
        val player = player()
        val box = if (spell.endsWith("bolt") || spell.endsWith("blast")) "magical_blastbox" else "celestial_surgebox"
        val catalytic = when (spell) {
            "spell_bolt" -> "chaos_rune"
            "spell_wave" -> "blood_rune"
            else -> "death_rune"
        }
        setItems(Item("air_rune", 1, def = ItemDefinition.EMPTY), Item(catalytic, 1, def = ItemDefinition.EMPTY))
        addItemDef(ItemDefinition(stringId = box, extras = mapOf("charges" to 1234, "charge_start" to 2)))

        player.equipment.set(EquipSlot.Shield.index, box)

        assertTrue(player.hasSpellItems(spell))
        assertEquals(2, Degrade.charges(player, player.equipment, EquipSlot.Shield.index))
        assertTrue(player.removeSpellItems(spell))
        assertEquals(1, Degrade.charges(player, player.equipment, EquipSlot.Shield.index))
    }

    @ParameterizedTest
    @ValueSource(strings = ["law", "nature"])
    fun `Dungeoneering staff without charges uses runes`(type: String) {
        val player = player()
        setItems(Item("air_rune", 2, def = ItemDefinition.EMPTY), Item("${type}_rune", 1, def = ItemDefinition.EMPTY))
        addItemDef(ItemDefinition(stringId = "${type}_staff", extras = mapOf("charges" to 0)))

        player.inventory.add("air_rune", 10)
        player.inventory.add("${type}_rune", 10)
        player.equipment.set(EquipSlot.Weapon.index, "${type}_staff")

        assertTrue(player.removeSpellItems("spell"))
        assertEquals(8, player.inventory.count("air_rune"))
        assertEquals(9, player.inventory.count("${type}_rune"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["law", "nature"])
    fun `Dungeoneering staff uses charges not runes`(type: String) {
        val player = player()
        setItems(Item("air_rune", 1, def = ItemDefinition.EMPTY), Item("${type}_rune", 2, def = ItemDefinition.EMPTY))
        addItemDef(ItemDefinition(stringId = "${type}_staff", extras = mapOf("charges" to 10)))

        player.inventory.add("air_rune", 10)
        player.inventory.add("${type}_rune", 10)
        player.equipment.set(EquipSlot.Weapon.index, "${type}_staff")

        assertTrue(player.removeSpellItems("spell"))
        assertEquals(9, player.inventory.count("air_rune"))
        assertEquals(10, player.inventory.count("${type}_rune"))
        assertEquals(8, Degrade.charges(player, player.equipment, EquipSlot.Weapon.index))
    }

    @ParameterizedTest
    @ValueSource(strings = ["law", "nature"])
    fun `Dungeoneering staff in inventory uses runes`(type: String) {
        val player = player()
        setItems(Item("air_rune", 1, def = ItemDefinition.EMPTY), Item("${type}_rune", 2, def = ItemDefinition.EMPTY))
        addItemDef(ItemDefinition(stringId = "${type}_staff", extras = mapOf("charges" to 10)))

        player.inventory.add("air_rune", 10)
        player.inventory.add("${type}_rune", 10)
        player.inventory.add("${type}_staff")

        assertTrue(player.removeSpellItems("spell"))
        assertEquals(9, player.inventory.count("air_rune"))
        assertEquals(8, player.inventory.count("${type}_rune"))
        assertEquals(10, Degrade.charges(player, player.inventory, 2))
    }

    @ParameterizedTest
    @ValueSource(strings = ["law", "nature"])
    fun `Dungeoneering staff can randomly save runes`(type: String) {
        setRandom(object : FakeRandom() {
            override fun nextInt(from: Int, until: Int) = 0
        })
        val player = player()
        setItems(Item("${type}_rune", 1, def = ItemDefinition.EMPTY))
        addItemDef(ItemDefinition(stringId = "${type}_staff", extras = mapOf("charges" to 10)))
        player.equipment.set(EquipSlot.Weapon.index, "${type}_staff")

        assertTrue(player.removeSpellItems("spell"))
        assertEquals(10, Degrade.charges(player, player.equipment, EquipSlot.Weapon.index))
    }
}