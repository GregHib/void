package content.skill.crafting

import WorldTest
import containsMessage
import interfaceOption
import itemOnObject
import net.pearx.kasechange.toLowerSpaceCase
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.client.ui.event.IntEntered
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class JewelleryTest : WorldTest() {

    private val moulds = listOf("ring", "necklace", "amulet_unstrung", "bracelet")
    private val gems = listOf("sapphire", "emerald", "ruby", "diamond", "dragonstone", "onyx")

    @TestFactory
    fun `Make gold jewellery`() = moulds.map { type ->
        dynamicTest("Make gold ${type.toLowerSpaceCase()}") {
            val player = createPlayer("Crafter", Tile(3227, 3255))
            player.levels.set(Skill.Crafting, 10)
            player.inventory.add("${type}_mould", "gold_bar")
            val furnace = objects[Tile(3226, 3256), "furnace_lumbridge"]!!

            player.itemOnObject(furnace, 0, "${type}_mould")
            tick()

            player.interfaceOption("make_mould_slayer", "make_${type}_option_gold", "Make 1")
            tick(3)

            assertEquals(0, player.inventory.count("gold_bar"))
            assertEquals(1, player.inventory.count("${type}_mould"))
            assertEquals(1, player.inventory.count("gold_${type}"))
            assertNotEquals(0.0, player.experience.get(Skill.Crafting))
        }
    }
    @Test
    fun `Make ring of slaying`() {
        val player = createPlayer("Crafter", Tile(3227, 3255))
        player.levels.set(Skill.Crafting, 75)
        player.inventory.add("ring_mould", "gold_bar", "enchanted_gem")
        val furnace = objects[Tile(3226, 3256), "furnace_lumbridge"]!!

        player.itemOnObject(furnace, 0, "ring_mould")
        tick()

        player.interfaceOption("make_mould_slayer", "make_ring_option_enchanted_gem", "Make 1")
        tick(3)

        assertEquals(0, player.inventory.count("gold_bar"))
        assertEquals(1, player.inventory.count("ring_mould"))
        assertEquals(1, player.inventory.count("ring_of_slaying_8"))
        assertEquals(15.0, player.experience.get(Skill.Crafting))
    }

    @TestFactory
    fun `Make gem jewellery`() = gems.flatMap { gem ->
        moulds.map { type ->
            dynamicTest("Make $gem ${type.toLowerSpaceCase()}") {
                val player = createPlayer("Crafter", Tile(3227, 3255))
                player.levels.set(Skill.Crafting, 99)
                player.inventory.add("${type}_mould", "gold_bar", gem)
                val furnace = objects[Tile(3226, 3256), "furnace_lumbridge"]!!

                player.itemOnObject(furnace, 0, "${type}_mould")
                tick()

                player.interfaceOption("make_mould_slayer", "make_${type}_option_${gem}", "Make 1")
                tick(3)

                assertEquals(0, player.inventory.count("gold_bar"))
                assertEquals(0, player.inventory.count(gem))
                assertEquals(1, player.inventory.count("${type}_mould"))
                assertEquals(1, player.inventory.count("${gem}_${type}"))
                assertNotEquals(0.0, player.experience.get(Skill.Crafting))
            }
        }
    }

    @Test
    fun `Can't make gem jewellery without a gem`() {
        val player = createPlayer("Crafter", Tile(3227, 3255))
        player.levels.set(Skill.Crafting, 99)
        player.inventory.add("necklace_mould", "gold_bar")
        val furnace = objects[Tile(3226, 3256), "furnace_lumbridge"]!!

        player.itemOnObject(furnace, 0, "necklace_mould")
        tick()

        player.interfaceOption("make_mould_slayer", "make_necklace_option_emerald", "Make 1")
        tick(3)

        assertEquals(1, player.inventory.count("gold_bar"))
        assertEquals(0, player.inventory.count("emerald"))
        assertEquals(1, player.inventory.count("necklace_mould"))
        assertEquals(0, player.inventory.count("emerald_necklace"))
        assertEquals(0.0, player.experience.get(Skill.Crafting))
    }

    @Test
    fun `Make 5 with more than in inventory`() {
        val player = createPlayer("Crafter", Tile(3227, 3255))
        player.levels.set(Skill.Crafting, 99)
        player.inventory.add("bracelet_mould")
        player.inventory.add("gold_bar", 6)
        player.inventory.add("ruby", 6)
        val furnace = objects[Tile(3226, 3256), "furnace_lumbridge"]!!

        player.itemOnObject(furnace, 0, "bracelet_mould")
        tick()

        player.interfaceOption("make_mould_slayer", "make_bracelet_option_ruby", "Make 5")
        tick(12)
        assertEquals(2, player.inventory.count("gold_bar"))
        assertEquals(2, player.inventory.count("ruby"))
        tick(3)

        assertEquals(1, player.inventory.count("gold_bar"))
        assertEquals(1, player.inventory.count("bracelet_mould"))
        assertEquals(1, player.inventory.count("ruby"))
        assertEquals(5, player.inventory.count("ruby_bracelet"))
        assertEquals(400.0, player.experience.get(Skill.Crafting))
    }

    @Test
    fun `Make All with extra gold bar`() {
        val player = createPlayer("Crafter", Tile(3227, 3255))
        player.levels.set(Skill.Crafting, 99)
        player.inventory.add("bracelet_mould")
        player.inventory.add("gold_bar", 3)
        player.inventory.add("ruby", 2)
        val furnace = objects[Tile(3226, 3256), "furnace_lumbridge"]!!

        player.itemOnObject(furnace, 0, "bracelet_mould")
        tick()

        player.interfaceOption("make_mould_slayer", "make_bracelet_option_ruby", "Make All")
        tick(6)
        assertEquals(1, player.inventory.count("gold_bar"))
        assertEquals(1, player.inventory.count("bracelet_mould"))
        assertEquals(0, player.inventory.count("ruby"))
        assertEquals(2, player.inventory.count("ruby_bracelet"))
        assertNotEquals(0.0, player.experience.get(Skill.Crafting))
    }

    @Test
    fun `Make X with missing gem`() {
        val player = createPlayer("Crafter", Tile(3227, 3255))
        player.levels.set(Skill.Crafting, 99)
        player.inventory.add("bracelet_mould")
        player.inventory.add("gold_bar", 3)
        player.inventory.add("diamond", 2)
        val furnace = objects[Tile(3226, 3256), "furnace_lumbridge"]!!

        player.itemOnObject(furnace, 0, "bracelet_mould")
        tick()

        player.interfaceOption("make_mould_slayer", "make_bracelet_option_diamond", "Make X")
        player.emit(IntEntered(3))
        tick(6)
        assertEquals(1, player.inventory.count("gold_bar"))
        assertEquals(1, player.inventory.count("bracelet_mould"))
        assertEquals(0, player.inventory.count("diamond"))
        assertEquals(2, player.inventory.count("diamond_bracelet"))
        assertNotEquals(0.0, player.experience.get(Skill.Crafting))
        assertTrue(player.containsMessage("You need some diamond in order to make a diamond bracelet."))
    }

    @Test
    fun `Make X more than has resources for`() {
        val player = createPlayer("Crafter", Tile(3227, 3255))
        player.levels.set(Skill.Crafting, 99)
        player.inventory.add("amulet_unstrung_mould")
        player.inventory.add("gold_bar", 1)
        player.inventory.add("dragonstone", 2)
        val furnace = objects[Tile(3226, 3256), "furnace_lumbridge"]!!

        player.itemOnObject(furnace, 0, "amulet_unstrung_mould")
        tick()

        player.interfaceOption("make_mould_slayer", "make_amulet_unstrung_option_dragonstone", "Make X")
        player.emit(IntEntered(3))
        tick(6)
        assertEquals(0, player.inventory.count("gold_bar"))
        assertEquals(1, player.inventory.count("amulet_unstrung_mould"))
        assertEquals(1, player.inventory.count("dragonstone"))
        assertEquals(1, player.inventory.count("dragonstone_amulet_unstrung"))
        assertNotEquals(0.0, player.experience.get(Skill.Crafting))
        assertTrue(player.containsMessage("You need some gold bars in order to make a dragonstone amulet unstrung."))
    }

    @Test
    fun `Can't make without correct crafting level`() {
        val player = createPlayer("Crafter", Tile(3227, 3255))
        player.levels.set(Skill.Crafting, 65)
        player.inventory.add("ring_mould", "gold_bar", "onyx")
        val furnace = objects[Tile(3226, 3256), "furnace_lumbridge"]!!

        player.itemOnObject(furnace, 0, "ring_mould")
        tick()

        player.interfaceOption("make_mould_slayer", "make_ring_option_onyx", "Make 1")
        tick(3)

        assertEquals(1, player.inventory.count("gold_bar"))
        assertEquals(1, player.inventory.count("ring_mould"))
        assertEquals(1, player.inventory.count("onyx"))
        assertEquals(0, player.inventory.count("onyx_ring"))
        assertEquals(0.0, player.experience.get(Skill.Crafting))
        assertTrue(player.containsMessage("You need to have a Crafting level of 67"))
    }
}