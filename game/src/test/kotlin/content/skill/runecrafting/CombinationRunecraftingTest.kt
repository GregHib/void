package content.skill.runecrafting

import FakeRandom
import WorldTest
import content.entity.obj.ObjectTeleports
import itemOnObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.koin.test.get
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Rune
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.charges
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.setRandom

internal class CombinationRunecraftingTest : WorldTest() {

    private lateinit var teleports: ObjectTeleports
    private lateinit var definitions: ItemDefinitions
    private val combinationsList = mutableListOf<List<Any>>()

    @BeforeEach
    fun setup() {
        setRandom(object : FakeRandom() {
            override fun nextBoolean() = false
        })
        teleports = get()
        definitions = get()
        combinationsList.clear()
        elements.flatMap { element ->
            val combinations = definitions.get("${element}_rune").get<Rune>("runecrafting").combinations
            combinations.map { (objectElement, value) ->
                val type = value[0] as String
                val xp = value[1] as Double
                combinationsList.add(listOf(element, objectElement, type, xp))
            }
        }
    }

    @TestFactory
    fun `Craft combination runes with binding necklace and magic imbue`() = combinationsList.map { (element, objectElement, type, xp) ->
        dynamicTest("Craft $type runes with $element runes on $objectElement altar") {
            val tile = teleports.get("${objectElement}_altar_ruins_enter", "Enter").first().to
            val player = createPlayer(tile)
            player.levels.set(Skill.Runecrafting, 99)
            player.equipment.set(EquipSlot.Amulet.index, "binding_necklace")
            player["binding_necklace_charges"] = 10
            player.start("magic_imbue", 10)
            player.inventory.add("${element}_rune")
            player.inventory.add("pure_essence")
            player.inventory.add("${element}_talisman")

            val altarTile = RunecraftingTest.altars.first { it.type == objectElement }.altarTile
            val altar = objects[altarTile, "${objectElement}_altar"]!!
            player.itemOnObject(altar, 0)
            tick(1)
            tickIf { player.visuals.moved }

            assertFalse(player.inventory.contains("pure_essence"))
            assertTrue(player.inventory.contains("${type}_rune"))
            assertTrue(player.inventory.contains("${element}_talisman"))
            assertEquals(xp, player.experience.get(Skill.Runecrafting))
            assertEquals(9, player.equipment.charges(player, EquipSlot.Amulet.index))
        }
    }

    @TestFactory
    fun `Combination runes have a chance of failing`() = combinationsList.map { (element, objectElement, type) ->
        dynamicTest("Craft $type runes with $element runes on $objectElement altar") {
            val tile = teleports.get("${objectElement}_altar_ruins_enter", "Enter").first().to
            val player = createPlayer(tile)
            player.levels.set(Skill.Runecrafting, 99)
            player.inventory.add("${element}_rune")
            player.inventory.add("pure_essence")
            player.inventory.add("${element}_talisman", 2)

            val altarTile = RunecraftingTest.altars.first { it.type == objectElement }.altarTile
            val altar = objects[altarTile, "${objectElement}_altar"]!!
            player.itemOnObject(altar, 0)
            tick(1)
            tickIf { player.visuals.moved }

            assertFalse(player.inventory.contains("pure_essence"))
            assertFalse(player.inventory.contains("${type}_rune"))
            assertEquals(1, player.inventory.count("${element}_talisman"))
            assertEquals(0.0, player.experience.get(Skill.Runecrafting))
        }
    }

    companion object {
        val elements = listOf("air", "water", "earth", "fire")
        val list = listOf("steam", "mist", "dust", "smoke", "mud", "lava")
    }
}
