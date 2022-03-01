package world.gregs.voidps.engine.client.update.task

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.client.update.task.player.PlayerChangeTask
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.LocalChange
import world.gregs.voidps.engine.entity.character.update.visual.player.movementType
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.network.visual.MoveType
import java.util.*

internal class PlayerChangeTaskTest : KoinMock() {

    override val modules = listOf(eventModule, entityListModule)

    lateinit var task: PlayerChangeTask

    @BeforeEach
    fun setup() {
        task = PlayerChangeTask(SequentialIterator(), mockk(relaxed = true))
        mockkStatic("world.gregs.voidps.engine.entity.character.move.MovementKt")
        mockkStatic("world.gregs.voidps.engine.entity.character.update.visual.player.MovementTypeKt")
        mockkStatic("world.gregs.voidps.engine.entity.character.update.visual.player.TemporaryMoveTypeKt")
    }

    @Test
    fun `Local update walk`() {
        // Given
        val player: Player = mockk(relaxed = true)
        every { player.movement.path.steps } returns LinkedList<Direction>()
        every { player.movement.walkStep } returns Direction.EAST
        every { player.movement.runStep } returns Direction.NONE
        every { player.movement.delta } returns Delta(1, 0)
        every { player.movementType } returns MoveType.Walk
        every { player.change } returns LocalChange.Walk
        // When
        task.run(player)
        // Then
        verifyOrder {
            player.change = LocalChange.Walk
            player.changeValue = 4
        }
    }

    @Test
    fun `Local update run`() {
        // Given
        val player: Player = mockk(relaxed = true)
        every { player.movement.path.steps } returns LinkedList<Direction>()
        every { player.movement.walkStep } returns Direction.NORTH
        every { player.movement.runStep } returns Direction.NORTH
        every { player.movement.delta } returns Delta(0, 2)
        every { player.movementType } returns MoveType.Run
        every { player.change } returns LocalChange.Run
        // When
        task.run(player)
        // Then
        verifyOrder {
            player.change = LocalChange.Run
            player.changeValue = 13
        }
    }

    @Test
    fun `Local update tele`() {
        // Given
        val player: Player = mockk(relaxed = true)
        every { player.movement.path.steps } returns LinkedList<Direction>()
        every { player.movement.walkStep } returns Direction.NONE
        every { player.movement.runStep } returns Direction.NONE
        every { player.movement.delta } returns Delta(12, -11, 1)
        every { player.movementType } returns MoveType.Teleport
        every { player.change } returns LocalChange.Tele
        // When
        task.run(player)
        // Then
        verifyOrder {
            player.change = LocalChange.Tele
            player.changeValue = 1429
        }
    }

    @Test
    fun `Local update tele far`() {
        // Given
        val player: Player = mockk(relaxed = true)
        every { player.movement.path.steps } returns LinkedList<Direction>()
        every { player.movement.walkStep } returns Direction.NONE
        every { player.movement.runStep } returns Direction.NONE
        every { player.movement.delta } returns Delta(247, -365, 1)
        every { player.movementType } returns MoveType.Teleport
        every { player.change } returns LocalChange.Tele
        // When
        task.run(player)
        // Then
        verifyOrder {
            player.change = LocalChange.TeleGlobal
            player.changeValue = 1779
        }
    }

    @Test
    fun `Local update visual`() {
        // Given
        val player: Player = mockk(relaxed = true)
        every { player.visuals.flag } returns 1
        every { player.movement.path.steps } returns LinkedList<Direction>()
        every { player.change } returns LocalChange.Update
        every { player.movementType } returns MoveType.None
        every { player.movement.delta } returns Delta.EMPTY
        // When
        task.run(player)
        // Then
        verifyOrder {
            player.change = LocalChange.Update
            player.changeValue = -1
        }
    }

    @Test
    fun `Local update no movement`() {
        // Given
        val player: Player = mockk(relaxed = true)
        every { player.movement.path.steps } returns LinkedList<Direction>()
        every { player.movement.walkStep } returns Direction.NONE
        every { player.movement.runStep } returns Direction.NONE
        every { player.movementType } returns MoveType.Teleport
        every { player.movement.delta } returns Delta(0, 0, 0)
        every { player.change } returns null
        // When
        task.run(player)
        // Then
        verifyOrder {
            player.change = null
            player.changeValue = -1
        }
    }

}