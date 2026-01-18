package content.area.wilderness.abyss

import WorldTest
import containsMessage
import objectOption
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class AbyssalRiftTest : WorldTest() {

    @TestFactory
    fun `Can't enter rifts without requirements`() = listOf(
        Tile(3028, 4837) to "Lost City",
        Tile(3049, 4839) to "armour",
        Tile(3050, 4837) to "strange power",
        Tile(3027, 4834) to "Legacy of Seergaze",
        Tile(3050, 4829) to "not yet unlocked",
    ).map { (tile, message) ->
        val obj = GameObjects.find(tile) { it.id.endsWith("rift") }
        dynamicTest("Can't enter ${obj.id}") {
            val player = createPlayer(tile)

            player.objectOption(obj, optionIndex = 0)
            tick(2)

            assertEquals(tile, player.tile)
            assertTrue(player.containsMessage(message))
        }
    }
}
