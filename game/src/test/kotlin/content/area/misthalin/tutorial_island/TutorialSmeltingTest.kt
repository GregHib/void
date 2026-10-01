package content.area.misthalin.tutorial_island

import WorldTest
import itemOnObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import skillCreation
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

/**
 * Smelting defers its transaction in a weak queue, and closing an interface clears weak
 * queues - so the instruction box must never take the chat box off a live dialogue.
 */
class TutorialSmeltingTest : WorldTest() {

    @Test
    fun `Smelting on the island produces a bronze bar`() {
        val player = createPlayer(Tile(3079, 9496)) { it.startTutorial(34) }
        player.inventory.add("tin_ore")
        player.inventory.add("copper_ore")
        val furnace = createObject("furnace_tutorial_island", Tile(3078, 9495))

        player.itemOnObject(furnace, 0)
        tick(2)
        player.skillCreation("Bronze bar", 1)
        tick(10)

        assertTrue(player.inventory.contains("bronze_bar"), "no bar: ${player.inventory.items.toList().filter { it.isNotEmpty() }}")
        assertEquals(35, player.tutorialStage)
    }

    @Test
    fun `Smelting works for a player outside the tutorial`() {
        val player = createPlayer(Tile(3079, 9496))
        player.inventory.add("tin_ore")
        player.inventory.add("copper_ore")
        val furnace = createObject("furnace_tutorial_island", Tile(3078, 9495))

        player.itemOnObject(furnace, 0)
        tick(2)
        player.skillCreation("Bronze bar", 1)
        tick(10)

        assertTrue(player.inventory.contains("bronze_bar"), "control: no bar either")
    }

    private fun Player.startTutorial(stage: Int) {
        set("tutorial_stage", stage)
        set("tutorial_designed", true)
    }
}
