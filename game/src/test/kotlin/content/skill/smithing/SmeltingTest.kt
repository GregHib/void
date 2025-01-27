package content.skill.smithing

import FakeRandom
import WorldTest
import net.pearx.kasechange.toSentenceCase
import objectOption
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import skillCreation
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class SmeltingTest : WorldTest() {

    private val bars = mapOf(
        "bronze" to listOf(Item("copper_ore", 1), Item("tin_ore", 1)),
        "blurite" to listOf(Item("blurite_ore", 1)),
        "iron" to listOf(Item("iron_ore", 1)),
        "silver" to listOf(Item("silver_ore", 1)),
        "steel" to listOf(Item("iron_ore", 1), Item("coal", 2)),
        "gold" to listOf(Item("gold_ore", 1)),
        "mithril" to listOf(Item("mithril_ore", 1), Item("coal", 4)),
        "adamant" to listOf(Item("adamantite_ore", 1), Item("coal", 6)),
        "rune" to listOf(Item("runite_ore", 1), Item("coal", 8)),
    )

    @TestFactory
    fun `Smelt bars`() = bars.map { (type, ores) ->
        dynamicTest("Smelt $type bar") {
            val player = createPlayer("Smelter", Tile(3227, 3255))
            player.levels.set(Skill.Smithing, 99)
            player.inventory.add(ores)
            val furnace = objects[Tile(3226, 3256), "furnace_lumbridge"]!!

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
        val player = createPlayer("Smelter", Tile(3227, 3255))
        player.levels.set(Skill.Smithing, 99)
        player.inventory.add("iron_ore", 2)
        player.inventory.add("coal", 3)
        val furnace = objects[Tile(3226, 3256), "furnace_lumbridge"]!!

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
        val player = createPlayer("Smelter", Tile(3227, 3255))
        player.levels.set(Skill.Smithing, 99)
        player.inventory.add("iron_ore", 2)
        val furnace = objects[Tile(3226, 3256), "furnace_lumbridge"]!!

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
        val player = createPlayer("Smelter", Tile(3109, 3502))
        player.equipment.set(EquipSlot.Hands.index, "goldsmith_gauntlets")
        player.levels.set(Skill.Smithing, 99)
        player.inventory.add("gold_ore")
        val furnace = objects[Tile(3110, 3502), "furnace_edgeville"]!!

        player.objectOption(furnace, "Smelt")
        tick()

        player.skillCreation("Gold bar", 2)
        tick(4)

        assertEquals(0, player.inventory.count("gold_ore"))
        assertEquals(1, player.inventory.count("gold_bar"))
        assertEquals(56.2, player.experience.get(Skill.Smithing))
    }

    @TestFactory
    fun `Smelt multiple bars at once with varrock armour`() = bars.toMutableMap().apply {
        remove("blurite")
        remove("silver")
        remove("gold")
    }.map { (type, ores) ->
        dynamicTest("Smelt multiple $type bars at once") {
            val player = createPlayer("Smelter", Tile(3109, 3502))
            player.equipment.set(EquipSlot.Chest.index, "varrock_armour_4")
            player.levels.set(Skill.Smithing, 99)
            player.inventory.add(ores)
            player.inventory.add(ores)
            val furnace = objects[Tile(3110, 3502), "furnace_edgeville"]!!

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
        val player = createPlayer("Smelter", Tile(3109, 3502))
        player.equipment.set(EquipSlot.Chest.index, "varrock_armour_4")
        player.levels.set(Skill.Smithing, 99)
        player.inventory.add("iron_ore", 2)
        val furnace = objects[Tile(3110, 3502), "furnace_edgeville"]!!

        player.objectOption(furnace, "Smelt")
        tick()

        player.skillCreation("Iron bar", 1)
        tick(4)

        assertEquals(1, player.inventory.count("iron_ore"))
        assertEquals(1, player.inventory.count("iron_bar"))
        assertEquals(12.5, player.experience.get(Skill.Smithing))
    }
}