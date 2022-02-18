package world.gregs.voidps.engine.client.update.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.update.task.npc.NPCChangeTask
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.update.LocalChange
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.script.KoinMock

internal class NPCChangeTaskTest : KoinMock() {

    override val modules = listOf(eventModule, entityListModule)

    lateinit var task: NPCChangeTask

    @BeforeEach
    fun setup() {
        task = NPCChangeTask(SequentialIterator(), mockk(relaxed = true))
    }

    @Test
    fun `Local update walk`() {
        // Given
        val npc: NPC = mockk(relaxed = true)
        every { npc.movement.walkStep } returns Direction.EAST
        every { npc.movement.runStep } returns Direction.NONE
        every { npc.movement.delta } returns Delta(1, 0)
        every { npc.def["crawl", false] } returns false
        every { npc.change } returns LocalChange.Walk
        // When
        task.run(npc)
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
        every { npc.movement.delta } returns Delta(1, 0)
        every { npc.def["crawl", false] } returns true
        every { npc.change } returns LocalChange.Crawl
        // When
        task.run(npc)
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
        every { npc.movement.delta } returns Delta(0, 2)
        every { npc.def["crawl", false] } returns false
        every { npc.change } returns LocalChange.Run
        // When
        task.run(npc)
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
        every { npc.movement.delta } returns Delta(247, -365, 1)
        every { npc.def["crawl", false] } returns false
        every { npc.change } returns LocalChange.Tele
        // When
        task.run(npc)
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
        every { npc.def["crawl", false] } returns false
        every { npc.movement.delta } returns Delta.EMPTY
        // When
        task.run(npc)
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
        every { npc.def["crawl", false] } returns false
        every { npc.visuals.update } returns null
        every { npc.movement.delta } returns Delta(0, 0)
        every { npc.change } returns null
        // When
        task.run(npc)
        // Then
        verifyOrder {
            npc.change = null
        }
    }

}