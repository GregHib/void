package world.gregs.voidps.engine.client.update.task

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.anyValue
import world.gregs.voidps.engine.client.update.task.npc.NPCMovementTask
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCMoveType
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Viewport
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.collision.CollisionStrategy
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.value
import java.util.*

internal class NPCMovementTaskTest : KoinMock() {

    override val modules = listOf(eventModule, entityListModule)

    private lateinit var task: NPCMovementTask
    private lateinit var movement: Movement
    private lateinit var npcs: NPCs
    private lateinit var npc: NPC
    private lateinit var path: Path

    @BeforeEach
    fun setup() {
        movement = mockk(relaxed = true)
        npcs = mockk(relaxed = true)
        npc = mockk(relaxed = true)
        path = mockk(relaxed = true)
        task = NPCMovementTask(npcs, mockk(relaxed = true))
        every { npc.movement } returns movement
        every { movement.path } returns path
        mockkStatic("world.gregs.voidps.engine.entity.character.move.MovementKt")
        mockkObject(SmallTraversal)
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
        every { path.steps } returns steps
        every { movement.frozen } returns true
        // When
        task.run()
        // Then
        assertEquals(1, steps.count())
    }

    @Test
    fun `Walk step`() {
        // Given
        val collision: CollisionStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        every { npc.running } returns false
        every { path.steps } returns steps
        every { movement.moving } returns true
        every { SmallTraversal.blocked(collision, anyValue(), Size.ONE, Direction.NORTH) } returns false
        every { npc.movementType = any() } just Runs
        // When
        task.run()
        // Then
        verifyOrder {
            movement.walkStep = Direction.NORTH
            movement.delta = Direction.NORTH.delta
            npc.movementType = NPCMoveType.Walk
        }
        assertEquals(1, steps.count())
    }

    @Test
    fun `Walk ignored if blocked`() {
        // Given
        val collision: CollisionStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        every { path.steps } returns steps
        every { SmallTraversal.blocked(collision, anyValue(), Size.ONE, Direction.NORTH) } returns true
        every { npc.movementType = any() } just Runs
        every { npc.running } returns false
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
        val collision: CollisionStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        every { path.steps } returns steps
        every { SmallTraversal.blocked(collision, anyValue(), Size.ONE, Direction.NORTH) } returns true
        every { npc.movementType = any() } just Runs
        every { npc.running } returns true
        every { movement.delta } returns value(Direction.NORTH.delta)
        // When
        task.run()
        // Then
        verify(exactly = 0) {
            movement.runStep = Direction.NORTH
            movement.delta = Delta(0, 2, 0)
            npc.movementType = NPCMoveType.Run
        }
    }

    @Test
    fun `Run step`() {
        // Given
        val collision: CollisionStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        every { path.steps } returns steps
        every { movement.moving } returns true
        every { SmallTraversal.blocked(collision, anyValue(), Size.ONE, Direction.NORTH) } returns false
        every { npc.movementType = any() } just Runs
        every { npc.running } returns true
        every { movement.delta } returns value(Direction.NORTH.delta)
        every { movement.delta = any() } just Runs
        // When
        task.run()
        // Then
        verifyOrder {
            movement.walkStep = Direction.NORTH
            movement.delta = Direction.NORTH.delta
            npc.movementType = NPCMoveType.Walk
            movement.runStep = Direction.NORTH
            movement.delta = Delta(0, 2, 0)
            npc.movementType = NPCMoveType.Run
        }
        assertEquals(1, steps.count())
    }

    @Test
    fun `Run odd step walks`() {
        // Given
        val viewport: Viewport = mockk(relaxed = true)
        val collision: CollisionStrategy = mockk(relaxed = true)
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        every { path.steps } returns steps
        every { movement.moving } returns true
        every { viewport.loaded } returns true
        every { SmallTraversal.blocked(collision, anyValue(), Size.ONE, Direction.NORTH) } returns false
        every { npc.movementType = any() } just Runs
        every { npc.running } returns true
        every { movement.delta } returns value(Direction.NORTH.delta)
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
            movement.delta = Delta(0, 2, 0)
        }
    }

}