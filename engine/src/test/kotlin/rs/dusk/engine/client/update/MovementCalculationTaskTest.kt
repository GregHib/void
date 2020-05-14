package rs.dusk.engine.client.update

import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.entity.list.entityListModule
import rs.dusk.engine.model.entity.index.Changes.Companion.RUN
import rs.dusk.engine.model.entity.index.Changes.Companion.TELE
import rs.dusk.engine.model.entity.index.Changes.Companion.UPDATE
import rs.dusk.engine.model.entity.index.Changes.Companion.WALK
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.update.visual.player.MovementType.Companion.TELEPORT
import rs.dusk.engine.model.entity.index.update.visual.player.movementType
import rs.dusk.engine.model.world.Tile
import rs.dusk.engine.script.KoinMock
import rs.dusk.engine.task.MovementCalculationTask

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 03, 2020
 */
internal class MovementCalculationTaskTest : KoinMock() {

    override val modules = listOf(entityListModule)

    lateinit var task: MovementCalculationTask

    @BeforeEach
    fun setup() {
        task = MovementCalculationTask()
    }

    @Test
    fun `Local update walk`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val value = 1337
        every { player.movement.direction } returns value
        every { player.movement.delta } returns Tile(1, 0)
        every { player.movementType } returns WALK
        every { player.changes.localUpdate } returns WALK
        // When
        runBlocking {
            task.updatePlayer(player).await()
        }
        // Then
        verifyOrder {
            val changes = player.changes
            changes.localUpdate = WALK
            changes.localValue = value
        }
    }

    @Test
    fun `Local update run`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val value = 420
        every { player.movement.direction } returns value
        every { player.movement.delta } returns Tile(0, 2)
        every { player.movementType } returns RUN
        every { player.changes.localUpdate } returns RUN
        // When
        runBlocking {
            task.updatePlayer(player).await()
        }
        // Then
        verifyOrder {
            val changes = player.changes
            changes.localUpdate = RUN
            changes.localValue = value
        }
    }

    @Test
    fun `Local update tele`() {
        // Given
        val player: Player = mockk(relaxed = true)
        every { player.movement.direction } returns -1
        every { player.movement.delta } returns Tile(247, -365, 1)
        every { player.movementType } returns TELEPORT
        every { player.changes.localUpdate } returns TELE
        // When
        runBlocking {
            task.updatePlayer(player).await()
        }
        // Then
        verifyOrder {
            val changes = player.changes
            changes.localUpdate = TELE
            changes.localValue = 1779
        }
    }

    @Test
    fun `Local update visual`() {
        // Given
        val player: Player = mockk(relaxed = true)
        every { player.changes.localUpdate } returns UPDATE
        every { player.movementType } returns -1
        // When
        runBlocking {
            task.updatePlayer(player).await()
        }
        // Then
        verifyOrder {
            val changes = player.changes
            changes.localUpdate = UPDATE
            changes.localValue = -1
        }
    }

    @Test
    fun `Local update no movement`() {
        // Given
        val player: Player = mockk(relaxed = true)
        every { player.movement.direction } returns 0
        every { player.movementType } returns TELEPORT
        every { player.visuals.update } returns null
        every { player.movement.delta } returns Tile(0)
        every { player.changes.localUpdate } returns -1
        // When
        runBlocking {
            task.updatePlayer(player).await()
        }
        // Then
        verifyOrder {
            val changes = player.changes
            changes.localUpdate = -1
            changes.localValue = -1
        }
    }

}