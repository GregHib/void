package content.area.misthalin.tutorial_island

import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.type.Tile

class TutorialStartingDoorTest : WorldTest() {
    private val doorway = Tile(3098, 3107)
    private val inside = Tile(3097, 3107)

    @Test
    fun `Guide steps must finish before a player can leave`() {
        val player = createPlayer(inside) { it.startTutorial(0) }
        val door = createObject("door_87_closed", doorway, ObjectShape.WALL_STRAIGHT, 0)
        player.objectOption(door, "Open")
        tick(6)
        assertEquals(inside, player.tile)
        assertEquals(0, player.tutorialStage)
        assertNotNull(GameObjects.findOrNull(doorway, "door_87_closed"))
    }

    @Test
    fun `Eligible passage closes promptly and cannot be followed by an early-stage account`() {
        val ready = createPlayer(inside) { it.startTutorial(3) }
        val early = createPlayer(Tile(3096, 3107)) { it.startTutorial(0) }
        val door = createObject("door_87_closed", doorway, ObjectShape.WALL_STRAIGHT, 0)
        ready.objectOption(door, "Open")
        tick()
        assertEquals(true, Collisions[3098, 3107, 0] and CollisionFlag.WALL_WEST != 0)
        early.walkTo(Tile(3099, 3107))
        tick(8)
        assertEquals(4, ready.tutorialStage)
        assertEquals(doorway, ready.tile)
        assertEquals(0, early.tutorialStage)
        assertEquals(true, early.tile.x < doorway.x)
        assertNotNull(GameObjects.findOrNull(doorway, "door_87_closed"))
    }

    @Test
    fun `An account stranded outside can return without skipping its guide`() {
        val player = createPlayer(doorway) { it.startTutorial(0) }
        val door = createObject("door_87_closed", doorway, ObjectShape.WALL_STRAIGHT, 0)
        player.objectOption(door, "Open")
        tick(8)
        assertEquals(inside, player.tile)
        assertEquals(0, player.tutorialStage)
        assertNotNull(GameObjects.findOrNull(doorway, "door_87_closed"))
    }

    private fun Player.startTutorial(stage: Int) {
        set("tutorial_stage", stage)
        set("tutorial_designed", true)
    }
}
