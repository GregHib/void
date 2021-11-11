package world.gregs.voidps.engine.client.update.task

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.anyValue
import world.gregs.voidps.engine.client.update.task.player.PlayerMovementTask
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerMoveType
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.Viewport
import world.gregs.voidps.engine.entity.character.update.visual.player.movementType
import world.gregs.voidps.engine.entity.character.update.visual.player.temporaryMoveType
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.path.traverse.TileTraversalStrategy
import world.gregs.voidps.engine.script.KoinMock
import java.util.*

internal class PlayerMovementTaskTest : KoinMock() {

    override val modules = listOf(eventModule, entityListModule)

    lateinit var task: PlayerMovementTask
    lateinit var movement: Movement
    lateinit var players: Players
    lateinit var player: Player
    lateinit var viewport: Viewport
    lateinit var path: Path

    @BeforeEach
    fun setup() {
        movement = mockk(relaxed = true)
        players = mockk(relaxed = true)
        player = mockk(relaxed = true)
        viewport = mockk(relaxed = true)
        path = mockk(relaxed = true)
        task = PlayerMovementTask(players, mockk(relaxed = true))
        every { player.movement } returns movement
        every { movement.path } returns path
        mockkStatic("world.gregs.voidps.engine.entity.character.move.MovementKt")
        every { players.forEach(any()) } answers {
            val action: (Player) -> Unit = arg(0)
            action.invoke(player)
        }
        every { player.viewport } returns viewport
    }

    @Test
    fun `Steps ignored if frozen`() {
        // Given
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        every { path.steps } returns steps
        every { movement.frozen } returns true
        every { viewport.loaded } returns true
        // When
        task.run()
        // Then
        assertEquals(1, steps.count())
    }

    @Test
    fun `Steps ignored if viewport not loaded`() {
        // Given
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        every { path.steps } returns steps
        every { viewport.loaded } returns false
        // When
        task.run()
        // Then
        assertEquals(1, steps.count())
    }

    @Test
    fun `Walk step`() {
        // Given
        val traversal: TileTraversalStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        mockkStatic("world.gregs.voidps.engine.entity.character.update.visual.player.MovementType")
        mockkStatic("world.gregs.voidps.engine.entity.character.update.visual.player.TemporaryMoveType")
        every { player.running } returns false
        every { path.steps } returns steps
        every { movement.traversal } returns traversal
        every { movement.moving } returns true
        every { viewport.loaded } returns true
        every { traversal.blocked(anyValue(), Direction.NORTH) } returns false
        // When
        task.run()
        // Then
        verifyOrder {
            movement.walkStep = Direction.NORTH
            movement.delta = Direction.NORTH.delta
            player.movementType = PlayerMoveType.Walk
            player.temporaryMoveType = PlayerMoveType.Walk
        }
        assertEquals(1, steps.count())
    }

    @Test
    fun `Walk ignored if blocked`() {
        // Given
        val traversal: TileTraversalStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        every { path.steps } returns steps
        every { movement.traversal } returns traversal
        every { viewport.loaded } returns true
        every { traversal.blocked(anyValue(), Direction.NORTH) } returns true
        every { player.running } returns false
        // When
        task.run()
        // Then
        verify(exactly = 0) {
            movement.walkStep = Direction.NORTH
            movement.delta = Direction.NORTH.delta
        }
    }

    @Test
    fun `Run ignored if blocked`() {
        // Given
        val traversal: TileTraversalStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        mockkStatic("world.gregs.voidps.engine.entity.character.update.visual.player.MovementType")
        mockkStatic("world.gregs.voidps.engine.entity.character.update.visual.player.TemporaryMoveType")
        every { path.steps } returns steps
        every { movement.traversal } returns traversal
        every { viewport.loaded } returns true
        every { traversal.blocked(anyValue(), Direction.NORTH) } returns true
        every { player.running } returns true
        every { movement.delta } returns Direction.NORTH.delta
        // When
        task.run()
        // Then
        verify(exactly = 0) {
            movement.runStep = Direction.NORTH
            movement.delta = Delta(0, 2, 0)
            player.movementType = PlayerMoveType.Run
            player.temporaryMoveType = PlayerMoveType.Run
        }
    }

    @Test
    fun `Run step`() {
        // Given
        val traversal: TileTraversalStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        mockkStatic("world.gregs.voidps.engine.entity.character.update.visual.player.MovementType")
        mockkStatic("world.gregs.voidps.engine.entity.character.update.visual.player.TemporaryMoveType")
        every { path.steps } returns steps
        every { movement.traversal } returns traversal
        every { movement.moving } returns true
        every { viewport.loaded } returns true
        every { traversal.blocked(anyValue(), Direction.NORTH) } returns false
        every { player.running } returns true
        every { movement.delta } returns Direction.NORTH.delta
        // When
        task.run()
        // Then
        verifyOrder {
            movement.walkStep = Direction.NORTH
            movement.delta = Direction.NORTH.delta
            player.movementType = PlayerMoveType.Walk
            player.temporaryMoveType = PlayerMoveType.Walk
            movement.runStep = Direction.NORTH
            movement.delta = Delta(0, 2, 0)
            player.movementType = PlayerMoveType.Run
            player.temporaryMoveType = PlayerMoveType.Run
        }
        assertEquals(1, steps.count())
    }

    @Test
    fun `Run odd step walks`() {
        // Given
        val traversal: TileTraversalStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        mockkStatic("world.gregs.voidps.engine.entity.character.update.visual.player.MovementType")
        mockkStatic("world.gregs.voidps.engine.entity.character.update.visual.player.TemporaryMoveType")
        every { path.steps } returns steps
        every { movement.traversal } returns traversal
        every { movement.moving } returns true
        every { viewport.loaded } returns true
        every { traversal.blocked(anyValue(), Direction.NORTH) } returns false
        every { player.running } returns true
        every { movement.delta } returns Direction.NORTH.delta
        // When
        task.run()
        // Then
        verifyOrder {
            movement.walkStep = Direction.NORTH
            movement.delta = Direction.NORTH.delta
            player.movementType = PlayerMoveType.Walk
            player.temporaryMoveType = PlayerMoveType.Walk
            player.movementType = PlayerMoveType.Walk
            player.temporaryMoveType = PlayerMoveType.Run
        }
        verify(exactly = 0) {
            movement.runStep = Direction.NORTH
            movement.delta = Delta(0, 2, 0)
        }
    }

}