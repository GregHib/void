package content.area.misthalin.varrock.grand_exchange

import WorldTest
import npcOption
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals

class DecantTest : WorldTest() {

    private data class TestCase(val name: String, val type: String, val startDoses: List<Int>, val expectedDoses: List<Int>)

    @TestFactory
    fun `Decant potions`() = listOf(
        TestCase("simple combine", "energy_potion", listOf(3, 2), listOf(4, 1)), // 3-dose + 2-dose -> 4-dose + 1-dose
        TestCase("exact fill", "super_attack", listOf(2, 2), listOf(4)), // 2x 2-dose -> 4-dose
        TestCase("untouched", "defence_potion", listOf(4), listOf(4)), // no changes
        TestCase("many partials", "hunter_potion", List(7) { 1 }, listOf(4, 3)), // 7x 1-dose -> 4-dose + 3-dose
        TestCase("mixed full + partial", "zamorak_brew", listOf(4, 1, 3), List(2) { 4 }), // 4-dose + 1-dose + 3-dose -> 2x 4-dose
        TestCase("single potion", "extreme_magic", listOf(3), listOf(3)), // no changes
        TestCase("empty", "potion", emptyList(), emptyList()), // empty
        TestCase("large stack", "antipoison++", List(20) { 1 }, List(5) { 4 }), // 20x1-dose -> 5x4-dose
        TestCase("off by one", "combat_potion", List(5) { 3 }, listOf(4, 4, 4, 3)), // 5x 3-dose -> 3x 4-dose + 3-dose
        TestCase("mixed", "prayer_potion", listOf(4, 3, 2, 1, 1), listOf(4, 4, 3)), // 4-dose + 3 dose + 2-dose + 2x 1-dose -> 2x 4-dose + 3-dose
    ).map { (name, type, start, expectedDoses) ->
        dynamicTest("Decant $name $type") {
            val player = createPlayer(Tile(3156, 3482))
            val bob = createNPC("bob_barter", Tile(3156, 3481))

            for (dose in start) {
                player.inventory.add("${type}_$dose")
            }

            player.npcOption(bob, "Decant")
            tick()

            for ((dose, amount) in expectedDoses.groupBy { it }) {
                assertEquals(amount.count(), player.inventory.count("${type}_$dose"), "${type}_$dose")
            }
        }
    }

    @TestFactory
    fun `Combine jewellery`() = listOf(
        TestCase("simple combine", "amulet_of_glory", listOf(3, 2), listOf(4, 1)), // 3-charge + 2-charge -> 4-charge + 1-charge
        TestCase("larger combine", "dig_site_pendant", listOf(3, 2), listOf(5)), // 3-charge + 2-charge -> 5-charge
        TestCase("exact fill", "games_necklace", listOf(2, 2), listOf(4)), // 2x 2-charge -> 4-charge
        TestCase("untouched", "ring_of_duelling", listOf(4), listOf(4)), // no changes
        TestCase("many partials into one", "ring_of_duelling", List(7) { 1 }, listOf(7)), // 7x 1-charge -> 7-charge
        TestCase("many partials", "amulet_of_glory", List(7) { 1 }, listOf(4, 3)), // 7x 1-charge -> 4-charge + 3-charge
        TestCase("mixed full + partial", "amulet_of_glory", listOf(4, 1, 3), List(2) { 4 }), // 4-charge + 1-charge + 3-charge -> 2x 4-charge
        TestCase("single potion", "amulet_of_glory", listOf(3), listOf(3)), // no changes
        TestCase("large stack", "ring_of_slaying", List(20) { 1 }, listOf(8, 8, 4)), // 20x1-charge -> 2x8-charge + 4-charge
        TestCase("off by one", "ring_of_slaying", List(16) { if (it == 0) 1 else 2 }, listOf(8, 8, 8, 7)), // 15x 2-charge + 1-charge -> 3x 8-charge + 7-charge
        TestCase("mixed", "games_necklace", listOf(4, 3, 2, 1, 1), listOf(8, 3)), // 4-charge + 3 charge + 2-charge + 2x 1-charge -> 8-charge + 3-charge
    ).map { (name, type, startDoses, expectedDoses) ->
        dynamicTest("Combine $name $type") {
            val player = createPlayer(Tile(3156, 3482))
            val bob = createNPC("murky_matt", Tile(3156, 3481))

            for (dose in startDoses) {
                player.inventory.add("${type}_$dose")
            }

            player.npcOption(bob, "Combine")
            tick()

            for ((dose, amount) in expectedDoses.groupBy { it }) {
                assertEquals(amount.count(), player.inventory.count("${type}_$dose"), "${type}_$dose")
            }
        }
    }
}
