package content.area.misthalin.tutorial_island

import WorldTest
import objectOption
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.type.Tile

class TutorialPassageTest : WorldTest() {
    @ParameterizedTest
    @CsvSource(
        "gate_72_closed, gate_73_closed, 3089, 3092, 3089, 3091, 2, 13, false",
        "gate_72_closed, gate_73_closed, 3089, 3092, 3089, 3091, 2, 13, true",
        "gate_73_closed, gate_72_closed, 3089, 3091, 3089, 3092, 2, 13, false",
        "gate_73_closed, gate_72_closed, 3089, 3091, 3089, 3092, 2, 13, true",
        "door_88_closed, none, 3079, 3084, 0, 0, 0, 14, false",
        "door_88_closed, none, 3079, 3084, 0, 0, 0, 14, true",
        "door_89_closed, none, 3072, 3090, 0, 0, 2, 19, false",
        "door_89_closed, none, 3072, 3090, 0, 0, 2, 19, true",
        "door_90_closed, none, 3086, 3126, 0, 0, 3, 23, false",
        "door_90_closed, none, 3086, 3126, 0, 0, 3, 23, true",
        "gate_74_closed, gate_75_closed, 3094, 9503, 3094, 9502, 2, 38, false",
        "gate_74_closed, gate_75_closed, 3094, 9503, 3094, 9502, 2, 38, true",
        "gate_75_closed, gate_74_closed, 3094, 9502, 3094, 9503, 2, 38, false",
        "gate_75_closed, gate_74_closed, 3094, 9502, 3094, 9503, 2, 38, true",
        "gate_76_closed, gate_77_closed, 3110, 9519, 3110, 9518, 2, 46, false",
        "gate_76_closed, gate_77_closed, 3110, 9519, 3110, 9518, 2, 46, true",
        "gate_77_closed, gate_76_closed, 3110, 9518, 3110, 9519, 2, 46, false",
        "gate_77_closed, gate_76_closed, 3110, 9518, 3110, 9519, 2, 46, true",
        "door_91_closed, none, 3125, 3124, 0, 0, 0, 53, false",
        "door_91_closed, none, 3125, 3124, 0, 0, 0, 53, true",
        "door_92_closed, none, 3130, 3124, 0, 0, 0, 55, false",
        "door_92_closed, none, 3130, 3124, 0, 0, 0, 55, true",
        "door_93_closed, none, 3122, 3102, 0, 0, 1, 62, false",
        "door_93_closed, none, 3122, 3102, 0, 0, 1, 62, true",
    )
    fun `Only eligible players cross and the passage closes with collision preserved`(
        id: String,
        otherId: String,
        x: Int,
        y: Int,
        otherX: Int,
        otherY: Int,
        rotation: Int,
        stage: Int,
        reverse: Boolean,
    ) {
        val tile = Tile(x, y)
        // Approach the boundary from its west/south side, independent of the tutorial's route.
        val west = rotation == 0 || rotation == 2
        val low = if (west) Tile(x - (if (rotation == 0) 1 else 0), y) else Tile(x, y - (if (rotation == 3) 1 else 0))
        val high = if (west) Tile(low.x + 1, low.y) else Tile(low.x, low.y + 1)
        val start = if (reverse) high else low
        val destination = if (reverse) low else high
        val early = createPlayer(start) { it.startTutorial(stage - 1) }
        val ready = createPlayer(start) { it.startTutorial(stage) }
        val door = createObject(id, tile, ObjectShape.WALL_STRAIGHT, rotation)
        if (otherId != "none") createObject(otherId, Tile(otherX, otherY), ObjectShape.WALL_STRAIGHT, rotation)
        val flag = when (rotation) {
            0 -> CollisionFlag.WALL_WEST
            1 -> CollisionFlag.WALL_NORTH
            2 -> CollisionFlag.WALL_EAST
            else -> CollisionFlag.WALL_SOUTH
        }
        early.objectOption(door, "Open")
        tick(6)
        assertEquals(start, early.tile)
        assertEquals(stage - 1, early.tutorialStage)
        assertNotNull(early.dialogue)
        ready.objectOption(door, "Open")
        tick()
        assertEquals(true, Collisions[x, y, 0] and flag != 0)
        early.walkTo(destination)
        tick(8)
        assertEquals(destination, ready.tile)
        assertEquals(stage + 1, ready.tutorialStage)
        assertEquals(stage - 1, early.tutorialStage)
        val position = if (west) early.tile.x else early.tile.y
        val boundary = if (west) destination.x else destination.y
        assertEquals(true, if (reverse) position > boundary else position < boundary)
        assertNotNull(GameObjects.findOrNull(tile, id))
        if (otherId != "none") assertNotNull(GameObjects.findOrNull(Tile(otherX, otherY), otherId))
    }

    private fun Player.startTutorial(stage: Int) {
        set("tutorial_stage", stage)
        set("tutorial_designed", true)
    }
}
