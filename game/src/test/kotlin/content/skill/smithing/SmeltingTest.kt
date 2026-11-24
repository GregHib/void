package content.skill.smithing

import FakeRandom
import WorldTest
import itemOption
import messages
import net.pearx.kasechange.toSentenceCase
import objectOption
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import skillCreation
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.setRandom
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SmeltingTest : WorldTest() {

    private val bars = mapOf(
        "bronze" to listOf(Item("copper_ore"), Item("tin_ore")),
        "blurite" to listOf(Item("blurite_ore")),
        "iron" to listOf(Item("iron_ore")),
        "silver" to listOf(Item("silver_ore")),
        "steel" to listOf(Item("iron_ore"), Item("coal", 2)),
        "gold" to listOf(Item("gold_ore")),
        "mithril" to listOf(Item("mithril_ore"), Item("coal", 4)),
        "adamant" to listOf(Item("adamantite_ore"), Item("coal", 6)),
        "rune" to listOf(Item("runite_ore"), Item("coal", 8)),
    )

    private fun forceSmeltingFailureRoll() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = until - 1
        })
    }

    @TestFactory
    fun `Smelt bars`() = bars.map { (type, ores) ->
        dynamicTest("Smelt $type bar") {
            val player = createPlayer(Tile(3227, 3255))
            player.levels.set(Skill.Smithing, 99)
            player.inventory.add(ores)
            val furnace = GameObjects.find(Tile(3226, 3256), "furnace_lumbridge")

            player.objectOption(furnace, "Smelt")
            tick()

            player.skillCreation("${type.toSentenceCase()} bar", 1)
            tick(4)

            for (ore in ores) {
                assertEquals(0, player.inventory.count(ore.id))
            }
            assertEquals(1, player.inventory.count("${type}_bar"))
            assertNotEquals(0.0, player.experience.get(Skill.Smithing))
        }
    }

    @Test
    fun `Smelt more than has resources for`() {
        val player = createPlayer(Tile(3227, 3255))
        player.levels.set(Skill.Smithing, 99)
        player.inventory.add("iron_ore", 2)
        player.inventory.add("coal", 3)
        val furnace = GameObjects.find(Tile(3226, 3256), "furnace_lumbridge")

        player.objectOption(furnace, "Smelt")
        tick()

        player.skillCreation("Steel bar", 2)
        tick(8)

        assertEquals(1, player.inventory.count("iron_ore"))
        assertEquals(1, player.inventory.count("coal"))
        assertEquals(1, player.inventory.count("steel_bar"))
        assertNotEquals(0.0, player.experience.get(Skill.Smithing))
    }

    @Test
    fun `Smelt more than one bar`() {
        val player = createPlayer(Tile(3227, 3255))
        player.levels.set(Skill.Smithing, 99)
        player.inventory.add("iron_ore", 2)
        val furnace = GameObjects.find(Tile(3226, 3256), "furnace_lumbridge")

        player.objectOption(furnace, "Smelt")
        tick()

        player.skillCreation("Iron bar", 2)
        tick(9)

        assertEquals(0, player.inventory.count("iron_ore"))
        assertEquals(2, player.inventory.count("iron_bar"))
        assertNotEquals(0.0, player.experience.get(Skill.Smithing))
    }

    @Test
    fun `Goldsmith gauntlets give bonus xp`() {
        val player = createPlayer(Tile(3109, 3502))
        player.equipment.set(EquipSlot.Hands.index, "goldsmith_gauntlets")
        player.levels.set(Skill.Smithing, 99)
        player.inventory.add("gold_ore")
        val furnace = GameObjects.find(Tile(3110, 3502), "furnace_edgeville")

        player.objectOption(furnace, "Smelt")
        tick()

        player.skillCreation("Gold bar", 2)
        tick(4)

        assertEquals(0, player.inventory.count("gold_ore"))
        assertEquals(1, player.inventory.count("gold_bar"))
        assertEquals(56.2, player.experience.get(Skill.Smithing))
    }

    @Test
    fun `Ring of forging guarantees iron bars`() {
        forceSmeltingFailureRoll()
        val player = createPlayer(Tile(3227, 3255))
        player.equipment.set(EquipSlot.Ring.index, "ring_of_forging")
        player.levels.set(Skill.Smithing, 99)
        player.inventory.add("iron_ore")
        val furnace = GameObjects.find(Tile(3226, 3256), "furnace_lumbridge")

        player.objectOption(furnace, "Smelt")
        tick()

        player.skillCreation("Iron bar", 1)
        tick(4)

        assertEquals(0, player.inventory.count("iron_ore"))
        assertEquals(1, player.inventory.count("iron_bar"))
    }

    @Test
    fun `Ring of forging check shows remaining charges`() {
        val player = createPlayer(Tile(3227, 3255))
        player["ring_of_forging_charges"] = 12
        player.equipment.set(EquipSlot.Ring.index, "ring_of_forging")

        player.itemOption(
            option = "Check",
            item = "ring_of_forging",
            id = "worn_equipment",
            component = "ring_slot",
            optionIndex = 1,
            inventory = "worn_equipment",
            slot = EquipSlot.Ring.index,
        )
        tick()

        assertTrue(player.messages.contains("You can smelt 128 more pieces of iron ore before a ring melts."))
    }

    @Test
    fun `Ring of forging does not guarantee iron bars when exhausted`() {
        forceSmeltingFailureRoll()
        val player = createPlayer(Tile(3227, 3255))
        player["ring_of_forging_charges"] = 140
        player.equipment.set(EquipSlot.Ring.index, "ring_of_forging")
        player.levels.set(Skill.Smithing, 99)
        player.inventory.add("iron_ore")
        val furnace = GameObjects.find(Tile(3226, 3256), "furnace_lumbridge")

        player.objectOption(furnace, "Smelt")
        tick()

        player.skillCreation("Iron bar", 1)
        tick(4)

        assertEquals(0, player.inventory.count("iron_ore"))
        assertEquals(0, player.inventory.count("iron_bar"))
        assertEquals("ring_of_forging", player.equipment[EquipSlot.Ring.index].id)
        assertEquals(140, player["ring_of_forging_charges", 0])
    }

    @Test
    fun `Ring of forging uses one charge when Varrock armour produces two bars`() {
        setRandom(object : FakeRandom() {
            override fun nextInt(until: Int) = 0
        })
        val player = createPlayer(Tile(3109, 3502))
        player["ring_of_forging_charges"] = 138
        player.equipment.set(EquipSlot.Ring.index, "ring_of_forging")
        player.equipment.set(EquipSlot.Chest.index, "varrock_armour_4")
        player.levels.set(Skill.Smithing, 99)
        player.inventory.add("iron_ore", 2)
        val furnace = GameObjects.find(Tile(3110, 3502), "furnace_edgeville")

        player.objectOption(furnace, "Smelt")
        tick()

        player.skillCreation("Iron bar", 2)
        tick(4)

        assertEquals(2, player.inventory.count("iron_bar"))
        assertEquals(139, player["ring_of_forging_charges", 0])
        assertEquals("ring_of_forging", player.equipment[EquipSlot.Ring.index].id)
    }

    @Test
    fun `Ring of forging melts only the worn ring after 140 bars`() {
        val player = createPlayer(Tile(3227, 3255))
        player["ring_of_forging_charges"] = 139
        player.equipment.set(EquipSlot.Ring.index, "ring_of_forging")
        player.inventory.add("ring_of_forging")
        player.levels.set(Skill.Smithing, 99)
        player.inventory.add("iron_ore")
        val furnace = GameObjects.find(Tile(3226, 3256), "furnace_lumbridge")

        player.objectOption(furnace, "Smelt")
        tick()

        player.skillCreation("Iron bar", 1)
        tick(4)

        assertEquals("", player.equipment[EquipSlot.Ring.index].id)
        assertEquals(1, player.inventory.count("ring_of_forging"))
        assertEquals(0, player["ring_of_forging_charges", 0])
        assertTrue(player.messages.contains("Your Ring of Forging has melted."))
    }

    @TestFactory
    fun `Smelt multiple bars at once with varrock armour`() = bars.toMutableMap().apply {
        remove("blurite")
        remove("silver")
        remove("gold")
    }.map { (type, ores) ->
        dynamicTest("Smelt multiple $type bars at once") {
            val player = createPlayer(Tile(3109, 3502))
            player.equipment.set(EquipSlot.Chest.index, "varrock_armour_4")
            player.levels.set(Skill.Smithing, 99)
            player.inventory.add(ores)
            player.inventory.add(ores)
            val furnace = GameObjects.find(Tile(3110, 3502), "furnace_edgeville")

            player.objectOption(furnace, "Smelt")
            tick()

            player.skillCreation("${type.toSentenceCase()} bar", 2)
            tick(4)

            for (ore in ores) {
                assertEquals(0, player.inventory.count(ore.id))
            }
            assertEquals(2, player.inventory.count("${type}_bar"))
            assertNotEquals(0.0, player.experience.get(Skill.Smithing))
        }
    }

    @Test
    fun `Don't smelt multiple bars if reached limit`() {
        val player = createPlayer(Tile(3109, 3502))
        player.equipment.set(EquipSlot.Chest.index, "varrock_armour_4")
        player.levels.set(Skill.Smithing, 99)
        player.inventory.add("iron_ore", 2)
        val furnace = GameObjects.find(Tile(3110, 3502), "furnace_edgeville")

        player.objectOption(furnace, "Smelt")
        tick()

        player.skillCreation("Iron bar", 1)
        tick(4)

        assertEquals(1, player.inventory.count("iron_ore"))
        assertEquals(1, player.inventory.count("iron_bar"))
        assertEquals(12.5, player.experience.get(Skill.Smithing))
    }
}
