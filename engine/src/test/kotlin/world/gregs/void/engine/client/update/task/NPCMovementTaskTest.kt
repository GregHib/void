package world.gregs.void.engine.client.update.task

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.void.engine.client.update.task.npc.NPCMovementTask
import world.gregs.void.engine.entity.Direction
import world.gregs.void.engine.entity.character.move.Movement
import world.gregs.void.engine.entity.character.npc.NPC
import world.gregs.void.engine.entity.character.npc.NPCMoveType
import world.gregs.void.engine.entity.character.npc.NPCs
import world.gregs.void.engine.entity.character.player.Viewport
import world.gregs.void.engine.entity.list.entityListModule
import world.gregs.void.engine.event.eventModule
import world.gregs.void.engine.map.Tile
import world.gregs.void.engine.path.TraversalStrategy
import world.gregs.void.engine.script.KoinMock
import java.util.*

/**
 * @author GregHib <greg@gregs.world>
 * @since May 29, 2020
 */
internal class NPCMovementTaskTest : KoinMock() {

    override val modules = listOf(eventModule, entityListModule)

    lateinit var task: NPCMovementTask
    lateinit var movement: Movement
    lateinit var npcs: NPCs
    lateinit var npc: NPC

    @BeforeEach
    fun setup() {
        movement = mockk(relaxed = true)
        npcs = mockk(relaxed = true)
        npc = mockk(relaxed = true)
        task = NPCMovementTask(npcs, mockk(relaxed = true))
        every { npcs.forEach(any()) } answers {
            val action: (NPC) -> Unit = arg(0)
            action.invoke(npc)
        }
    }

    @Test
    fun `Steps ignored if frozen`() {
        // Given
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        every { npc.movement } returns movement
        every { movement.steps } returns steps
        every { movement.frozen } returns true
        // When
        task.run()
        // Then
        assertEquals(1, steps.size)
    }

    @Test
    fun `Walk step`() {
        // Given
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        every { npc.movement } returns movement
        every { movement.steps } returns steps
        every { movement.traversal } returns traversal
        every { traversal.blocked(any(), Direction.NORTH) } returns false
        every { npc.movementType = any() } just Runs
        // When
        task.run()
        // Then
        verifyOrder {
            movement.walkStep = Direction.NORTH
            movement.delta = Direction.NORTH.delta
            npc.movementType = NPCMoveType.Walk
        }
        assertEquals(1, steps.size)
    }

    @Test
    fun `Walk ignored if blocked`() {
        // Given
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        every { npc.movement } returns movement
        every { movement.steps } returns steps
        every { movement.traversal } returns traversal
        every { traversal.blocked(any(), Direction.NORTH) } returns true
        every { npc.movementType = any() } just Runs
        every { movement.running } returns false
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
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        every { npc.movement } returns movement
        every { movement.steps } returns steps
        every { movement.traversal } returns traversal
        every { traversal.blocked(any(), Direction.NORTH) } returns true
        every { npc.movementType = any() } just Runs
        every { movement.running } returns true
        every { movement.delta } returns Direction.NORTH.delta
        // When
        task.run()
        // Then
        verify(exactly = 0) {
            movement.runStep = Direction.NORTH
            movement.delta = Tile(0, 2, 0)
            npc.movementType = NPCMoveType.Run
        }
    }

    @Test
    fun `Run step`() {
        // Given
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        every { npc.movement } returns movement
        every { movement.steps } returns steps
        every { movement.traversal } returns traversal
        every { traversal.blocked(any(), Direction.NORTH) } returns false
        every { npc.movementType = any() } just Runs
        every { movement.running } returns true
        every { movement.delta } returns Direction.NORTH.delta
        every { movement.delta = any() } just Runs
        // When
        task.run()
        // Then
        verifyOrder {
            movement.walkStep = Direction.NORTH
            movement.delta = Direction.NORTH.delta
            npc.movementType = NPCMoveType.Walk
            movement.runStep = Direction.NORTH
            movement.delta = Tile(0, 2, 0)
            npc.movementType = NPCMoveType.Run
        }
        assertEquals(1, steps.size)
    }

    @Test
    fun `Run odd step walks`() {
        // Given
        val viewport: Viewport = mockk(relaxed = true)
        val traversal: TraversalStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        every { npc.movement } returns movement
        every { movement.steps } returns steps
        every { movement.traversal } returns traversal
        every { viewport.loaded } returns true
        every { traversal.blocked(any(), Direction.NORTH) } returns false
        every { npc.movementType = any() } just Runs
        every { movement.running } returns true
        every { movement.delta } returns Direction.NORTH.delta
        every { movement.delta = any() } just Runs
        // When
        task.run()
        // Then
        verifyOrder {
            movement.walkStep = Direction.NORTH
            movement.delta = Direction.NORTH.delta
            npc.movementType = NPCMoveType.Walk
            npc.movementType = NPCMoveType.Walk
        }
        verify(exactly = 0) {
            movement.runStep = Direction.NORTH
            movement.delta = Tile(0, 2, 0)
        }
    }

}