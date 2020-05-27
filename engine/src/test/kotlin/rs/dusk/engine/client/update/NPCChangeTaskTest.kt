package rs.dusk.engine.client.update

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.update.task.NPCChangeTask
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.index.LocalChange
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.npc.NPCMoveType
import rs.dusk.engine.model.entity.list.entityListModule
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 15, 2020
 */
internal class NPCChangeTaskTest : KoinMock() {

    override val modules = listOf(entityListModule)

    lateinit var task: NPCChangeTask

    @BeforeEach
    fun setup() {
        task = NPCChangeTask(mockk(relaxed = true))
    }

    @Test
    fun `Local update walk`() {
        // Given
        val npc: NPC = mockk(relaxed = true)
        every { npc.movement.walkStep } returns Direction.EAST
        every { npc.movement.runStep } returns Direction.NONE
        every { npc.movement.delta } returns Tile(1, 0)
        every { npc.movementType } returns NPCMoveType.Walk
        every { npc.change } returns LocalChange.Walk
        // When
        task.runAsync(npc)
        // Then
        verifyOrder {
            npc.change = LocalChange.Walk
            npc.walkDirection = 2
        }
    }

    @Test
    fun `Local update crawl`() {
        // Given
        val npc: NPC = mockk(relaxed = true)
        every { npc.movement.walkStep } returns Direction.EAST
        every { npc.movement.runStep } returns Direction.NONE
        every { npc.movement.delta } returns Tile(1, 0)
        every { npc.movementType } returns NPCMoveType.Crawl
        every { npc.change } returns LocalChange.Crawl
        // When
        task.runAsync(npc)
        // Then
        verifyOrder {
            npc.change = LocalChange.Crawl
            npc.walkDirection = 2
        }
    }

    @Test
    fun `Local update run`() {
        // Given
        val npc: NPC = mockk(relaxed = true)
        every { npc.movement.walkStep } returns Direction.NORTH
        every { npc.movement.runStep } returns Direction.NORTH
        every { npc.movement.delta } returns Tile(0, 2)
        every { npc.movementType } returns NPCMoveType.Run
        every { npc.change } returns LocalChange.Run
        // When
        task.runAsync(npc)
        // Then
        verifyOrder {
            npc.change = LocalChange.Run
            npc.walkDirection = 0
            npc.runDirection = 0
        }
    }

    @Test
    fun `Local update tele`() {
        // Given
        val npc: NPC = mockk(relaxed = true)
        every { npc.movement.walkStep } returns Direction.NONE
        every { npc.movement.runStep } returns Direction.NONE
        every { npc.movement.delta } returns Tile(247, -365, 1)
        every { npc.movementType } returns NPCMoveType.Teleport
        every { npc.change } returns LocalChange.Tele
        // When
        task.runAsync(npc)
        // Then
        verifyOrder {
            npc.change = LocalChange.Tele
        }
    }

    @Test
    fun `Local update visual`() {
        // Given
        val npc: NPC = mockk(relaxed = true)
        every { npc.change } returns LocalChange.Update
        every { npc.movementType } returns NPCMoveType.None
        // When
        task.runAsync(npc)
        // Then
        verifyOrder {
            npc.change = LocalChange.Update
        }
    }

    @Test
    fun `Local update no movement`() {
        // Given
        val npc: NPC = mockk(relaxed = true)
        every { npc.movement.walkStep } returns Direction.NONE
        every { npc.movement.runStep } returns Direction.NONE
        every { npc.movementType } returns NPCMoveType.Teleport
        every { npc.visuals.update } returns null
        every { npc.movement.delta } returns Tile(0)
        every { npc.change } returns null
        // When
        task.runAsync(npc)
        // Then
        verifyOrder {
            npc.change = null
        }
    }

}