package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory

class CombinationRuneTest : MagicSpellTest() {

    @TestFactory
    fun `Remove combo runes`() = listOf(
        Triple("mist_rune", "air_rune", "water_rune"),
        Triple("dust_rune", "air_rune", "earth_rune"),
        Triple("mud_rune", "water_rune", "earth_rune"),
        Triple("smoke_rune", "air_rune", "fire_rune"),
        Triple("steam_rune", "water_rune", "fire_rune"),
        Triple("lava_rune", "earth_rune", "fire_rune")
    ).map { (combo, element1, element2) ->
        dynamicTest("Remove ${combo.replace("_", " ")}s") {
            Settings.load(mapOf("members" to "true"))
            val player = player()
            setItems(Item(element1, 2), Item(element2, 1), Item("chaos_rune", 1))

            player.inventory.add(combo, 10)
            player.inventory.add("chaos_rune", 10)

            assertTrue(player.removeSpellItems("spell"))
            assertEquals(8, player.inventory.count(combo))
            assertEquals(9, player.inventory.count("chaos_rune"))
            Settings.clear()
        }
    }

    @TestFactory
    fun `Use regular when out of combo runes`() = listOf(
        "mist_rune" to "air_rune",
        "mist_rune" to "water_rune",
        "dust_rune" to "air_rune",
        "dust_rune" to "earth_rune",
        "mud_rune" to "water_rune",
        "mud_rune" to "earth_rune",
        "smoke_rune" to "air_rune",
        "smoke_rune" to "fire_rune",
        "steam_rune" to "water_rune",
        "steam_rune" to "fire_rune",
        "lava_rune" to "earth_rune",
        "lava_rune" to "fire_rune"
    ).map { (combo, element) ->
        dynamicTest("Use ${element.replace("_", " ")}s when out of ${combo.replace("_", " ")}s") {
            Settings.load(mapOf("members" to "true"))
            val player = player()
            setItems(Item(element, 2))

            player.inventory.add(element, 10)
            player.inventory.add(combo, 1)

            assertTrue(player.removeSpellItems("spell"))
            assertEquals(9, player.inventory.count(element))
            assertEquals(0, player.inventory.count(combo))

            assertTrue(player.hasSpellItems("spell"))
            Settings.clear()
        }
    }
}