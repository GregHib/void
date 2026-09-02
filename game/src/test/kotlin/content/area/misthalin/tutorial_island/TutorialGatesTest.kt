package content.area.misthalin.tutorial_island

import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.type.Tile

/**
 * Both halves of each dungeon doorway exist on the map, so they have to be tested as the pair
 * they are - a single half takes a different code path in [content.entity.obj.door.DoubleDoor].
 */
class TutorialGatesTest : WorldTest() {

    @Test
    fun `Mining area doorway opens and lets the player through`() {
        val start = Tile(3093, 9503)
        val player = createPlayer(start) { it.startTutorial(38) }
        val door = createObject("gate_74_closed", Tile(3094, 9503), ObjectShape.WALL_STRAIGHT, rotation = 2)
        createObject("gate_75_closed", Tile(3094, 9502), ObjectShape.WALL_STRAIGHT, rotation = 2)

        player.objectOption(door, "Open")
        tick(6)

        assertEquals(39, player.tutorialStage)
        assertNotEquals(start, player.tile)
    }

    @Test
    fun `Rat pit doorway opens and lets the player through`() {
        val start = Tile(3109, 9519)
        val player = createPlayer(start) { it.startTutorial(46) }
        val door = createObject("gate_76_closed", Tile(3110, 9519), ObjectShape.WALL_STRAIGHT, rotation = 2)
        createObject("gate_77_closed", Tile(3110, 9518), ObjectShape.WALL_STRAIGHT, rotation = 2)

        player.objectOption(door, "Open")
        tick(6)

        assertEquals(47, player.tutorialStage)
        assertNotEquals(start, player.tile)
    }

    private fun Player.startTutorial(stage: Int) {
        set("tutorial_stage", stage)
        set("tutorial_designed", true)
    }
}
