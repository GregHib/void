package content.bot.behaviour.loadout

import content.bot.behaviour.loadout.LoadoutFixtures.gear
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.config.Config
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class GearLoaderTest {

    @BeforeEach
    fun setup() {
        GearDefinitions.set(
            gear("melee_weapons", "rune_scimitar", "steel_scimitar", "bronze_sword"),
            gear("shields", "rune_kiteshield", "wooden_shield"),
        )
    }

    @AfterEach
    fun teardown() {
        GearDefinitions.clear()
        LoadoutDefinitions.clear()
        ItemDefinitions.clear()
    }

    private fun readGear(toml: String): Map<String, Gear> {
        val map = mutableMapOf<String, Gear>()
        Config.stringReader(toml) { with(GearDefinitions) { readGear(map) } }
        return map
    }

    private fun readLoadouts(toml: String): Map<String, Loadout> {
        val map = mutableMapOf<String, Loadout>()
        Config.stringReader(toml) { with(LoadoutDefinitions) { readLoadouts(map) } }
        return map
    }

    @Test
    fun `Gear items are parsed in order`() {
        val map = readGear(
            """
            [melee_weapons]
            items = ["a", "b", "c"]

            [other]
            items = ["x"]
            """.trimIndent(),
        )
        assertEquals(listOf("a", "b", "c"), map.getValue("melee_weapons").ids)
        assertEquals(listOf("x"), map.getValue("other").ids)
    }

    @Test
    fun `Gear with no items throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            readGear("[empty]\nitems = []")
        }
    }

    @Test
    fun `Gear with unknown key throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            readGear("[bad]\nitems = [\"a\"]\nbogus = 1")
        }
    }

    @Test
    fun `Duplicate gear throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            readGear("[a]\nitems = [\"a\"]\n[a]\nitems = [\"b\"]")
        }
    }

    @Test
    fun `Gear items are resolved lazily from item definitions`() {
        LoadoutFixtures.define()
        val gear = gear("weapons", "rune_scimitar", "dragon_2h_sword")
        val first = gear.items.first()
        assertEquals(EquipSlot.Weapon, first.slot)
        assertFalse(first.twoHanded)
        assertEquals(1, first.requirements.size)
        assertTrue(gear.items[1].twoHanded)
        assertEquals(EquipSlot.Weapon, gear.slot)
    }

    @Test
    fun `Unknown item in gear throws when resolved`() {
        LoadoutFixtures.define()
        assertThrows(IllegalStateException::class.java) { gear("bad", "not_an_item").items }
    }

    @Test
    fun `Loadout slots are parsed`() {
        val map = readLoadouts(
            """
            [melee_basic]
            weapon = { gear = "melee_weapons" }
            shield = { gear = "shields", optional = true, min = 1 }
            cape = { id = "obsidian_cape,fire_cape" }
            inventory = [{ id = "shark", min = 5 }]
            autocast = "wind_strike"
            weight = 4
            """.trimIndent(),
        )
        val loadout = map.getValue("melee_basic")
        assertEquals("melee_weapons", loadout.slots.getValue(EquipSlot.Weapon).gear!!.id)
        assertFalse(loadout.slots.getValue(EquipSlot.Weapon).optional)
        assertEquals(1, loadout.slots.getValue(EquipSlot.Weapon).min)
        val shield = loadout.slots.getValue(EquipSlot.Shield)
        assertTrue(shield.optional)
        assertEquals("shields", shield.gear!!.id)
        val cape = loadout.slots.getValue(EquipSlot.Cape)
        assertNull(cape.gear)
        assertEquals(setOf("obsidian_cape", "fire_cape"), cape.ids)
        assertEquals(5, loadout.inventory.single().min)
        assertEquals(setOf("shark"), loadout.inventory.single().ids)
        assertEquals("wind_strike", loadout.autocast)
        assertEquals(4, loadout.weight)
    }

    @Test
    fun `Loadout defaults`() {
        val loadout = readLoadouts("[a]\nweapon = { gear = \"melee_weapons\" }").getValue("a")
        assertEquals(1, loadout.weight)
        assertNull(loadout.autocast)
        assertTrue(loadout.inventory.isEmpty())
        assertNull(loadout.extraInventory)
    }

    @Test
    fun `Loadout with nested equipment map`() {
        val loadout = readLoadouts(
            """
            [nested]
            equipment = { weapon = { gear = "melee_weapons" }, shield = { id = "wooden_shield" } }
            """.trimIndent(),
        ).getValue("nested")
        assertEquals(setOf(EquipSlot.Weapon, EquipSlot.Shield), loadout.slots.keys)
        assertEquals(setOf("wooden_shield"), loadout.slots.getValue(EquipSlot.Shield).ids)
    }

    @Test
    fun `Unknown gear id in loadout throws`() {
        assertThrows(IllegalStateException::class.java) {
            readLoadouts("[a]\nweapon = { gear = \"nope\" }")
        }
    }

    @Test
    fun `Invalid slot key throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            readLoadouts("[a]\nbogus_slot = { gear = \"melee_weapons\" }")
        }
    }

    @Test
    fun `Slot with both id and gear throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            readLoadouts("[a]\nweapon = { id = \"bronze_sword\", gear = \"melee_weapons\" }")
        }
    }

    @Test
    fun `Slot with neither id nor gear throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            readLoadouts("[a]\nweapon = { min = 1 }")
        }
    }

    @Test
    fun `Duplicate loadout throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            readLoadouts("[a]\nweapon = { gear = \"melee_weapons\" }\n[a]\nweapon = { gear = \"melee_weapons\" }")
        }
    }

    @Test
    fun `Loadout validate rejects gear for the wrong slot`() {
        LoadoutFixtures.define()
        GearDefinitions.set(gear("shields", "rune_kiteshield"))
        val loadout = readLoadouts("[a]\nweapon = { gear = \"shields\" }").getValue("a")
        assertThrows(IllegalArgumentException::class.java) { loadout.validate() }
    }
}
