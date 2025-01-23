package world.gregs.voidps.engine.entity.character.mode.move

import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.koin.test.mock.declareMock
import org.rsmod.game.pathfinder.*
import org.rsmod.game.pathfinder.collision.CollisionStrategies
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.mode.move.target.TileTargetStrategy
import world.gregs.voidps.engine.entity.character.move.previousTile
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.BodyParts
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.entity.character.player.temporaryMoveType
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.update.player.MoveType
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone

internal class MovementTest : KoinMock() {

    lateinit var player: Player
    lateinit var pathFinder: PathFinder
    lateinit var stepValidator: StepValidator

    @BeforeEach
    fun setup() {
        player = Player(tile = Tile(5, 5))
        player.collision = CollisionStrategies.Normal
        declareMock<AreaDefinitions> {
            every { get(any<Zone>()) } returns emptySet()
        }
        pathFinder = declareMock {
            every { findPath(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns Route(listOf(RouteCoordinates(10, 10)),
                alternative = false,
                success = true)
        }
        stepValidator = declareMock {
            every { canTravel(any(), any(), any(), any(), any(), any(), any(), any()) } returns true
        }
        declareMock<LineValidator> {
        }
    }

    @Test
    fun `Player queues smart route`() {
        player.tile = Tile(10, 10)
        val movement = Movement(player, TileTargetStrategy(Tile.EMPTY))
        movement.calculate()
        assertTrue(player.steps.isNotEmpty())
    }

    @Test
    fun `Npc queues step`() {
        val npc = NPC(tile = Tile(10, 10))
        val movement = Movement(npc, TileTargetStrategy(Tile.EMPTY))
        movement.calculate()
        assertTrue(npc.steps.isNotEmpty())
        verify(exactly = 0) {
            pathFinder.findPath(any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `Delayed player processes forced movement`() {
        player["delay"] = -1
        val movement = Movement(player)
        player.steps.queueStep(Tile(10, 10), noCollision = true)
        movement.tick()
        assertTrue(player.visuals.moved)
        assertEquals(1, player.visuals.walkStep)
        assertEquals(-1, player.visuals.runStep)
    }

    @TestFactory
    fun `Unloaded viewport isn't processed`() = listOf("unloaded", "frozen", "delayed").map { type ->
        dynamicTest("$type viewport isn't processed") {
            val movement = Movement(player)
            player.steps.queueStep(Tile(10, 10), noCollision = true)
            when (type) {
                "unloaded" -> player.viewport = Viewport()
                "frozen" -> player.start("movement_delay", -1)
                "delayed" -> player["delay"] = -1
            }
            movement.tick()
            assertFalse(player.visuals.moved)
            assertEquals(-1, player.visuals.walkStep)
        }
    }

    @Test
    fun `Walking takes one step`() {
        val movement = Movement(player)
        player.steps.queueSteps(listOf(Tile(6, 6)))
        player.running = false
        movement.tick()
        assertTrue(player.visuals.moved)
        assertEquals(1, player.visuals.walkStep)
        assertEquals(-1, player.visuals.runStep)
        assertEquals(MoveType.Walk, player.movementType)
        assertEquals(MoveType.Walk, player.temporaryMoveType)

        assertEquals(Direction.NORTH_EAST, player.direction)
        assertEquals(Tile(5, 5), player.previousTile)
        assertEquals(Tile(6, 6), player.tile)
    }

    @Test
    fun `Running takes two steps`() {
        val movement = Movement(player)
        player.steps.queueSteps(listOf(Tile(10, 10)))
        player.running = true
        movement.tick()
        assertTrue(player.visuals.moved)
        assertEquals(1, player.visuals.walkStep)
        assertEquals(1, player.visuals.runStep)

        assertEquals(Direction.NORTH_EAST, player.direction)
        assertEquals(Tile(6, 6), player.previousTile)
        assertEquals(Tile(7, 7), player.tile)
        assertFalse(player.steps.isEmpty())
    }

    @Test
    fun `Doesn't move if blocked`() {
        val movement = Movement(player)
        every { stepValidator.canTravel(any(), any(), any(), any(), any(), any(), any(), any()) } returns false
        player.steps.queueSteps(listOf(Tile(10, 10)))
        movement.tick()
        assertFalse(player.visuals.moved)
        assertEquals(-1, player.visuals.walkStep)
        assertEquals(-1, player.visuals.runStep)
    }

    @Test
    fun `Check recalculate when reach waypoint and target moved`() {
        var target = Tile(10, 10)
        val strategy = object : TargetStrategy {
            override val bitMask: Int = 0
            override val tile: Tile
                get() = target
            override val width: Int = 1
            override val height: Int = 1
            override val sizeX: Int = 1
            override val sizeY: Int = 1
            override val rotation: Int = 0
            override val exitStrategy: Int = 0
        }
        val movement = Movement(player, strategy)
        movement.calculate()
        target = Tile(1, 1)
        repeat(5) {
            movement.tick()
        }
        every { pathFinder.findPath(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns
                Route(listOf(RouteCoordinates(1, 1)), alternative = false, success = true)
        assertEquals(Tile(10, 10), player.tile)
        repeat(2) {
            movement.tick()
        }
        assertEquals(Tile(8, 8), player.tile)
    }

    @Test
    fun `Odd number of steps when running has a step`() {
        val movement = Movement(player)
        player.steps.queueSteps(listOf(Tile(8, 5)))
        player.running = true

        movement.tick()
        assertEquals(2, player.visuals.walkStep)
        assertEquals(2, player.visuals.runStep)
        assertEquals(MoveType.Run, player.movementType)
        assertEquals(MoveType.Run, player.temporaryMoveType)

        movement.tick()
        assertEquals(2, player.visuals.walkStep)
        assertEquals(2, player.visuals.runStep)
        assertEquals(MoveType.Walk, player.movementType)
        assertEquals(MoveType.Walk, player.temporaryMoveType)
    }
}