package rs.dusk.engine.client.update

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import rs.dusk.engine.entity.list.entityListModule
import rs.dusk.engine.entity.model.Changes.Companion.ADJACENT_REGION
import rs.dusk.engine.entity.model.Changes.Companion.GLOBAL_REGION
import rs.dusk.engine.entity.model.Changes.Companion.HEIGHT
import rs.dusk.engine.entity.model.Changes.Companion.NONE
import rs.dusk.engine.entity.model.Changes.Companion.RUN
import rs.dusk.engine.entity.model.Changes.Companion.TELE
import rs.dusk.engine.entity.model.Changes.Companion.WALK
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.visuals.player.MovementType.Companion.TELEPORT
import rs.dusk.engine.entity.model.visual.visuals.player.movementType
import rs.dusk.engine.model.Tile
import rs.dusk.engine.script.KoinMock

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 03, 2020
 */
internal class MovementCalculationTaskTest : KoinMock() {

    override val modules = listOf(entityListModule)

    lateinit var task: MovementCalculationTask

    @BeforeEach
    fun setup() {
        task = MovementCalculationTask(mockk(relaxed = true))
    }

    @TestFactory
    fun `Region update types`() = arrayOf(
        NONE to Tile(0, 0),
        HEIGHT to Tile(0, 0, 1),
        ADJACENT_REGION to Tile(64, 64),
        ADJACENT_REGION to Tile(0, 64),
        ADJACENT_REGION to Tile(64, 0),
        ADJACENT_REGION to Tile(-64, -64, 2),
        ADJACENT_REGION to Tile(0, -64),
        ADJACENT_REGION to Tile(-64, 0),
        GLOBAL_REGION to Tile(128, 128, 3),
        GLOBAL_REGION to Tile(0, 128),
        GLOBAL_REGION to Tile(128, 0),
        GLOBAL_REGION to Tile(-128, -128),
        GLOBAL_REGION to Tile(0, -128),
        GLOBAL_REGION to Tile(-128, 0)
    ).map { (expected, delta) ->
        dynamicTest("Region update for movement $delta") {
            // Given
            val player: Player = mockk(relaxed = true)
            every { player.movement.delta } returns delta
            every { player.movementType } returns 0
            // When
            runBlocking {
                task.updatePlayer(player).await()
            }
            // Then
            verify { player.changes.regionUpdate = expected }
        }
    }

    @TestFactory
    fun `Region update values`() = arrayOf(
        Triple(NONE, Tile(0, 0), -1),
        Triple(HEIGHT, Tile(0, 0, 1), 1),
        Triple(HEIGHT, Tile(0, 0, -3), -3),
        Triple(ADJACENT_REGION, Tile(-64, 64, 0), 0),
        Triple(ADJACENT_REGION, Tile(0, 64, 1), 9),
        Triple(ADJACENT_REGION, Tile(64, 64, 2), 18),
        Triple(ADJACENT_REGION, Tile(64, 0, 3), 28),
        Triple(ADJACENT_REGION, Tile(64, -64, 0), 7),
        Triple(ADJACENT_REGION, Tile(0, -64, 1), 14),
        Triple(ADJACENT_REGION, Tile(-64, -64, 2), 21),
        Triple(ADJACENT_REGION, Tile(-64, 0, 3), 27),
        Triple(GLOBAL_REGION, Tile(128, 128, 0), 514),
        Triple(GLOBAL_REGION, Tile(-128, -128, 1), 130814),
        Triple(GLOBAL_REGION, Tile(768, -1024, 2), 134384)
    ).map { (updateType, delta, expected) ->
        dynamicTest("Region value for movement $delta") {
            // Given
            val player: Player = mockk(relaxed = true)
            every { player.movement.delta } returns delta
            every { player.changes.regionUpdate } returns updateType
            every { player.movementType } returns 0
            // When
            runBlocking {
                task.updatePlayer(player).await()
            }
            // Then
            verify { player.changes.regionValue = expected }
        }
    }

    @Test
    fun `Local update walk`() {
        // Given
        val player: Player = mockk(relaxed = true)
        val value = 1337
        every { player.movement.direction } returns value
        every { player.movement.run } returns false
        every { player.movement.delta } returns Tile(1, 0)
        every { player.movementType } returns -1
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
        every { player.movement.run } returns true
        every { player.movement.delta } returns Tile(0, 2)
        every { player.movementType } returns -1
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
        every { player.changes.localUpdate } returns NONE
        every { player.movementType } returns -1
        // When
        runBlocking {
            task.updatePlayer(player).await()
        }
        // Then
        verifyOrder {
            val changes = player.changes
            changes.localUpdate = NONE
            changes.localValue = -1
        }
    }

    @Test
    fun `Local update no movement`() {
        // Given
        val player: Player = mockk(relaxed = true)
        every { player.movement.direction } returns 0
        every { player.movement.run } returns true
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