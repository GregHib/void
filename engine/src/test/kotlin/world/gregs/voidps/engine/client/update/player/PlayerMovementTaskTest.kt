/*
package world.gregs.voidps.engine.client.update.player

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import world.gregs.voidps.engine.client.update.MovementTask
import world.gregs.voidps.engine.client.update.iterator.SequentialIterator
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.entity.character.player.temporaryMoveType
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.event.EventHandlerStore
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.collision.CollisionStrategyProvider
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.visual.update.player.MoveType
import world.gregs.voidps.network.visual.update.player.MovementType

internal class PlayerMovementTaskTest : KoinMock() {

    override val modules = listOf(module {
        single { EventHandlerStore() }
        single { Players() }
        single { mockk<CollisionStrategyProvider>(relaxed = true) }
    })

    lateinit var task: MovementTask<Player>
    lateinit var movement: Movement
    lateinit var players: Players
    lateinit var player: Player
    lateinit var viewport: Viewport

    @BeforeEach
    fun setup() {
        mockkStatic("world.gregs.voidps.engine.entity.character.player.PlayerVisualExtensionsKt")
        mockkStatic("world.gregs.voidps.engine.entity.ValuesKt")
        movement = mockk(relaxed = true)
        players = mockk(relaxed = true)
        player = mockk(relaxed = true)
        viewport = mockk(relaxed = true)
        task = MovementTask(SequentialIterator(), players)
        every { player.movement } returns movement
        every { players.iterator() } returns mutableListOf(player).iterator()
        every { player.viewport } returns viewport
        every { player.visuals.movementType } returns MovementType()
    }

    @Test
    fun `Steps ignored if frozen`() {
        // Given
        every { movement.nextStep() } returns Direction.NORTH
        every { player.hasEffect("frozen") } returns true
        every { player.moving } returns true
        every { viewport.loaded } returns true
        // When
        task.run()
        // Then
        assertEquals(1, movement.steps.count())
    }

    @Test
    fun `Steps ignored if viewport not loaded`() {
        // Given
        every { movement.nextStep() } returns Direction.NORTH
        every { viewport.loaded } returns false
        // When
        task.run()
        // Then
        assertEquals(1, movement.steps.count())
    }

    @Test
    fun `Walk step`() {
        // Given
        every { movement.nextStep() } returns Direction.NORTH
        every { player.running } returns false
        every { player.moving } returns true
        every { viewport.loaded } returns true
        // When
        task.run()
        // Then
        verifyOrder {
            movement.step(Direction.NORTH, false)
            movement.delta = Direction.NORTH.delta
            player.movementType = MoveType.Walk
            player.temporaryMoveType = MoveType.Walk
        }
        assertEquals(1, movement.steps.count())
    }

    @Test
    fun `Walk ignored if blocked`() {
        // Given
        every { movement.nextStep() } returns Direction.NORTH
        every { viewport.loaded } returns true
        every { player.blocked(any(), Direction.NORTH) } returns true
        every { player.moving } returns false
        every { player.running } returns false
        // When
        task.run()
        // Then
        verify(exactly = 0) {
            player.visuals.walkStep = 0 // North
            movement.delta = Direction.NORTH.delta
        }
    }

    @Test
    fun `Run ignored if blocked`() {
        // Given
        every { movement.nextStep() } returns Direction.NORTH
        every { viewport.loaded } returns true
        every { player.blocked(any(), Direction.NORTH) } returns true
        every { player.moving } returns false
        every { player.running } returns true
        every { movement.delta } returns Direction.NORTH.delta
        // When
        task.run()
        // Then
        verify(exactly = 0) {
            player.visuals.runStep = 0 // North
            movement.delta = Delta(0, 2, 0)
            player.movementType = MoveType.Run
            player.temporaryMoveType = MoveType.Run
        }
    }

    @Test
    fun `Run step`() {
        // Given
        every { movement.nextStep() } returns Direction.NORTH
        every { player.moving } returns true
        every { viewport.loaded } returns true
        every { player.running } returns true
        every { movement.delta } returns Direction.NORTH.delta
        // When
        task.run()
        // Then
        verifyOrder {
            movement.step(Direction.NORTH, false)
            movement.delta = Direction.NORTH.delta
            player.movementType = MoveType.Walk
            player.temporaryMoveType = MoveType.Walk
            movement.step(Direction.NORTH, true)
            movement.delta = Delta(0, 2, 0)
            player.movementType = MoveType.Run
            player.temporaryMoveType = MoveType.Run
        }
        assertEquals(1, movement.steps.count())
    }

    @Test
    fun `Run odd step walks`() {
        // Given
        every { movement.nextStep() } returns Direction.NORTH
        every { player.moving } returns true
        every { viewport.loaded } returns true
        every { player.running } returns true
        every { movement.delta } returns Direction.NORTH.delta
        // When
        task.run()
        // Then
        verifyOrder {
            movement.step(Direction.NORTH, false)
            movement.delta = Direction.NORTH.delta
            player.movementType = MoveType.Walk
            player.temporaryMoveType = MoveType.Walk
            player.movementType = MoveType.Walk
            player.temporaryMoveType = MoveType.Run
        }
        verify(exactly = 0) {
            movement.step(Direction.NORTH, true)
            movement.delta = Delta(0, 2, 0)
        }
    }

}*/
