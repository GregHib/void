package world.gregs.voidps.engine.client.update.npc

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import world.gregs.voidps.engine.client.update.MovementTask
import world.gregs.voidps.engine.client.update.iterator.SequentialIterator
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.move.Path
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.path.traverse.SmallTraversal
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.value
import java.util.*

internal class NPCMovementTaskTest : KoinMock() {

    override val modules = listOf(eventModule, entityListModule, module {  })

    private lateinit var task: MovementTask<NPC>
    private lateinit var movement: Movement
    private lateinit var npcs: NPCs
    private lateinit var npc: NPC
    private lateinit var path: Path

    @BeforeEach
    fun setup() {
        mockkStatic("world.gregs.voidps.engine.entity.character.move.MovementKt")
        mockkStatic("world.gregs.voidps.engine.map.collision.CollisionStrategyKt")
        mockkStatic("world.gregs.voidps.engine.entity.ValuesKt")
        mockkObject(SmallTraversal)
        movement = mockk(relaxed = true)
        npcs = mockk(relaxed = true)
        npc = mockk(relaxed = true)
        path = mockk(relaxed = true)
        task = MovementTask(SequentialIterator(), npcs, mockk(relaxed = true))
        every { npc.movement } returns movement
        every { npc.def["swim", false] } returns false
        every { npc.def["fly", false] } returns false
        every { npc.def["crawl", false] } returns false
        every { movement.path } returns path
        every { npcs.iterator() } returns mutableListOf(npc).iterator()
        every { npc.blocked(any(), any()) } returns false
    }

    @Test
    fun `Steps ignored if frozen`() {
        // Given
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        every { path.steps } returns steps
        every { npc.moving } returns true
        every { npc.hasEffect("frozen") } returns true
        // When
        task.run()
        // Then
        assertEquals(1, steps.count())
    }

    @Test
    fun `Walk step`() {
        // Given
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        every { npc.running } returns false
        every { path.steps } returns steps
        every { npc.moving } returns true
        // When
        task.run()
        // Then
        verifyOrder {
            movement.step(Direction.NORTH, false)
            movement.delta = Direction.NORTH.delta
        }
        assertEquals(1, steps.count())
    }

    @Test
    fun `Walk ignored if blocked`() {
        // Given
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        every { path.steps } returns steps
        every { npc.blocked(any(), any()) } returns true
        every { npc.moving } returns false
        every { npc.running } returns false
        // When
        task.run()
        // Then
        verify(exactly = 0) {
            movement.step(Direction.NORTH, false)
            movement.delta = Direction.NORTH.delta
        }
    }

    @Test
    fun `Run ignored if blocked`() {
        // Given
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        every { path.steps } returns steps
        every { npc.blocked(any(), any()) } returns true
        every { npc.moving } returns false
        every { npc.running } returns true
        every { movement.delta } returns value(Direction.NORTH.delta)
        // When
        task.run()
        // Then
        verify(exactly = 0) {
            movement.step(Direction.NORTH, true)
            movement.delta = Delta(0, 2, 0)
        }
    }

    @Test
    fun `Run step`() {
        // Given
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        steps.add(Direction.NORTH)
        every { path.steps } returns steps
        every { npc.moving } returns true
        every { npc.running } returns true
        every { movement.delta } returns value(Direction.NORTH.delta)
        // When
        task.run()
        // Then
        verifyOrder {
            movement.step(Direction.NORTH, false)
            movement.delta = Direction.NORTH.delta
            movement.step(Direction.NORTH, true)
            movement.delta = Delta(0, 2, 0)
        }
        assertEquals(1, steps.count())
    }

    @Test
    fun `Run odd step walks`() {
        // Given
        val steps = LinkedList<Direction>()
        steps.add(Direction.NORTH)
        every { path.steps } returns steps
        every { npc.moving } returns true
        every { npc.running } returns true
        every { movement.delta } returns value(Direction.NORTH.delta)
        // When
        task.run()
        // Then
        verifyOrder {
            movement.step(Direction.NORTH, false)
            movement.delta = Direction.NORTH.delta
        }
        verify(exactly = 0) {
            movement.step(Direction.NORTH, true)
            movement.delta = Delta(0, 2, 0)
        }
    }

}