package rs.dusk.engine.client.update

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.client.update.task.PlayerChangeTask
import rs.dusk.engine.model.entity.Direction
import rs.dusk.engine.model.entity.index.LocalChange
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.player.PlayerMoveType
import rs.dusk.engine.model.entity.index.update.visual.player.movementType
import rs.dusk.engine.model.entity.list.entityListModule
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.script.KoinMock
import java.util.*

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 03, 2020
 */
internal class PlayerChangeTaskTest : KoinMock() {

    override val modules = listOf(entityListModule)

    lateinit var task: PlayerChangeTask

    @BeforeEach
    fun setup() {
        task = PlayerChangeTask(mockk(relaxed = true))
    }

    @Test
    fun `Local update walk`() {
        // Given
        val player: Player = mockk(relaxed = true)
        every { player.movement.steps } returns LinkedList()
        every { player.movement.walkStep } returns Direction.EAST
        every { player.movement.runStep } returns Direction.NONE
        every { player.movement.delta } returns Tile(1, 0)
        every { player.movementType } returns PlayerMoveType.Walk
        every { player.change } returns LocalChange.Walk
        // When
        task.runAsync(player)
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
        every { player.movement.steps } returns LinkedList()
        every { player.movement.walkStep } returns Direction.NORTH
        every { player.movement.runStep } returns Direction.NORTH
        every { player.movement.delta } returns Tile(0, 2)
        every { player.movementType } returns PlayerMoveType.Run
        every { player.change } returns LocalChange.Run
        // When
        task.runAsync(player)
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
        every { player.movement.steps } returns LinkedList()
        every { player.movement.walkStep } returns Direction.NONE
        every { player.movement.runStep } returns Direction.NONE
        every { player.movement.delta } returns Tile(247, -365, 1)
        every { player.movementType } returns PlayerMoveType.Teleport
        every { player.change } returns LocalChange.Tele
        // When
        task.runAsync(player)
        // Then
        verifyOrder {
            player.change = LocalChange.Tele
            player.changeValue = 1779
        }
    }

    @Test
    fun `Local update visual`() {
        // Given
        val player: Player = mockk(relaxed = true)
        every { player.movement.steps } returns LinkedList()
        every { player.change } returns LocalChange.Update
        every { player.movementType } returns PlayerMoveType.None
        // When
        task.runAsync(player)
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
        every { player.movement.steps } returns LinkedList()
        every { player.movement.walkStep } returns Direction.NONE
        every { player.movement.runStep } returns Direction.NONE
        every { player.movementType } returns PlayerMoveType.Teleport
        every { player.visuals.update } returns null
        every { player.movement.delta } returns Tile(0)
        every { player.change } returns null
        // When
        task.runAsync(player)
        // Then
        verifyOrder {
            player.change = null
            player.changeValue = -1
        }
    }

}