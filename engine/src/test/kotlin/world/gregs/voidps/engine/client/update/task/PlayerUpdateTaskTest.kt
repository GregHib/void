package world.gregs.voidps.engine.client.update.task

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.anyValue
import world.gregs.voidps.engine.client.update.task.player.PlayerUpdateTask
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerTrackingSet
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.Viewport
import world.gregs.voidps.engine.entity.character.update.LocalChange
import world.gregs.voidps.engine.entity.character.update.RegionChange
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.utility.toInt
import world.gregs.voidps.engine.value
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.updatePlayers

internal class PlayerUpdateTaskTest : KoinMock() {

    lateinit var task: PlayerUpdateTask
    lateinit var players: Players
    override val modules = listOf(
        eventModule,
        entityListModule
    )

    @BeforeEach
    fun setup() {
        players = mockk(relaxed = true)
        every { players.indexed(any()) } returns null
        task = spyk(PlayerUpdateTask(players))
    }

    @Test
    fun `Locals processed before global, active before idle`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        val viewport = mockk<Viewport>(relaxed = true)
        val entities = mockk<PlayerTrackingSet>(relaxed = true)
        every { player.viewport } returns viewport
        every { viewport.players } returns entities
        mockkStatic("world.gregs.voidps.network.encode.PlayerUpdateEncoderKt")
        val client: Client = mockk(relaxed = true)
        every { player.client } returns client
        every { client.updatePlayers(any(), any()) } just Runs
        every { task.processLocals(any(), any(), any(), any(), any()) } just Runs
        every { task.processGlobals(any(), any(), any(), any(), any()) } just Runs
        // When
        task.run(player)
        // Then
        verifyOrder {
            task.processLocals(any(), any(), entities, viewport, true)
            task.processLocals(any(), any(), entities, viewport, false)
            task.processGlobals(any(), any(), entities, viewport, true)
            task.processGlobals(any(), any(), entities, viewport, false)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Local idle player ignored on update`(active: Boolean) {
        // Given
        val activePlayer = mockk<Player>(relaxed = true)
        val idlePlayer = mockk<Player>(relaxed = true)
        val entities = mockk<PlayerTrackingSet>(relaxed = true)
        val viewport = mockk<Viewport>(relaxed = true)
        val idleIndex = 1
        val activeIndex = 2

        every { players.indexed(1) } returns idlePlayer
        every { players.indexed(2) } returns activePlayer
        every { entities.indices } returns (0 until 2)
        every { entities.locals } returns intArrayOf(1, 2)
        every { viewport.isIdle(idleIndex) } returns true
        every { viewport.isIdle(activeIndex) } returns false
        every { activePlayer.index } returns activeIndex
        every { idlePlayer.index } returns idleIndex
        every { idlePlayer.change } returns null
        every { activePlayer.change } returns null
        // When
        task.processLocals(mockk(relaxed = true), mockk(relaxed = true), entities, viewport, active)
        // Then
        verify(exactly = (!active).toInt()) { viewport.setIdle(idleIndex) }
        verify(exactly = active.toInt()) { viewport.setIdle(activeIndex) }
    }

    @Test
    fun `Local player removed`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        val viewport = mockk<Viewport>(relaxed = true)
        val entities = mockk<PlayerTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)

        val index = 0
        every { player.index } returns index
        every { players.indexed(1) } returns player
        every { entities.indices } returns (0 until 1)
        every { entities.locals } returns intArrayOf(1)
        every { entities.local(index) } returns true
        every { entities.remove(index) } returns true
        every { viewport.lastSeen } returns IntArray(1)
        // When
        task.processLocals(sync, mockk(relaxed = true), entities, viewport, true)
        // Then
        verify(exactly = 0) { viewport.setIdle(any()) }
        verifyOrder {
            sync.startBitAccess()
            sync.writeBits(1, true)
            sync.writeBits(1, false)// Even when update isn't null (relaxed)
            sync.writeBits(2, 0)
            task.encodeRegion(sync, viewport, player)
            sync.finishBitAccess()
        }
    }

    @TestFactory
    fun `Local player move on foot`() = arrayOf(LocalChange.Walk, LocalChange.Run).map { change ->
        dynamicTest("Local player ${change::class.simpleName}") {
            // Given
            val player = mockk<Player>(relaxed = true)
            val viewport = mockk<Viewport>(relaxed = true)
            val entities = mockk<PlayerTrackingSet>(relaxed = true)
            val sync: Writer = mockk(relaxed = true)
            val value = 1

            every { player.changeValue } returns value
            every { player.change } returns change
            every { players.indexed(1) } returns player
            every { entities.indices } returns (0 until 1)
            every { entities.locals } returns intArrayOf(1)
            // When
            task.processLocals(sync, mockk(relaxed = true), entities, viewport, true)
            // Then
            verify(exactly = 0) { viewport.setIdle(any()) }
            verifyOrder {
                sync.writeBits(1, true)
                sync.writeBits(1, true)
                sync.writeBits(2, change.id)
                sync.writeBits(change.id + 2, value)
            }
        }
    }

    @Test
    fun `Local player teleport`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        val viewport = mockk<Viewport>(relaxed = true)
        val entities = mockk<PlayerTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val value = 1

        every { player.changeValue } returns value
        every { player.change } returns LocalChange.Tele
        every { players.indexed(1) } returns player
        every { entities.indices } returns (0 until 1)
        every { entities.locals } returns intArrayOf(1)
        // When
        task.processLocals(sync, mockk(relaxed = true), entities, viewport, true)
        // Then
        verify(exactly = 0) { viewport.setIdle(any()) }
        verifyOrder {
            sync.writeBits(1, true)
            sync.writeBits(1, true)
            sync.writeBits(2, LocalChange.Tele.id)
            sync.writeBits(1, false)
            sync.writeBits(12, value)
        }
    }

    @Test
    fun `Local player visual update`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        val viewport = mockk<Viewport>(relaxed = true)
        val entities = mockk<PlayerTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)

        every { player.changeValue } returns -1
        every { player.change } returns LocalChange.Update
        every { players.indexed(1) } returns player
        every { entities.indices } returns (0 until 1)
        every { entities.locals } returns intArrayOf(1)
        // When
        task.processLocals(sync, updates, entities, viewport, true)
        // Then
        verify(exactly = 0) { viewport.setIdle(any()) }
        verifyOrder {
            sync.writeBits(1, true)
            sync.writeBits(1, true)
            sync.writeBits(2, RegionChange.Update.id)
            updates.writeBytes(player.visuals.update!!)
        }
    }

    @Test
    fun `Local player no visual update`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        val viewport = mockk<Viewport>(relaxed = true)
        val entities = mockk<PlayerTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)

        every { player.changeValue } returns -1
        every { player.change } returns LocalChange.Update
        every { player.visuals.update } returns null
        every { players.indexed(1) } returns player
        every { entities.indices } returns (0 until 1)
        every { entities.locals } returns intArrayOf(1)
        // When
        task.processLocals(sync, updates, entities, viewport, true)
        // Then
        verify(exactly = 0) {
            viewport.setIdle(any())
            updates.writeBytes(any<ByteArray>())
        }
        verifyOrder {
            sync.writeBits(1, true)
            sync.writeBits(1, false)
            sync.writeBits(2, RegionChange.Update.id)
        }
    }

    @Test
    fun `Local player skip`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        val skipPlayer = mockk<Player>(relaxed = true)
        val viewport = mockk<Viewport>(relaxed = true)
        val entities = mockk<PlayerTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val index = 1

        every { skipPlayer.index } returns index
        every { skipPlayer.change } returns null
        every { players.indexed(1) } returns skipPlayer
        every { players.indexed(2) } returns player
        every { entities.indices } returns (0 until 2)
        every { entities.locals } returns intArrayOf(1, 2)
        // When
        task.processLocals(sync, updates, entities, viewport, true)
        // Then
        verifyOrder {
            viewport.setIdle(index)
            task.writeSkip(sync, 0)
            sync.writeBits(1, true)
        }
    }

    @Test
    fun `Skip last local player`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        val viewport = mockk<Viewport>(relaxed = true)
        val entities = mockk<PlayerTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)

        every { player.change } returns null
        every { players.indexed(1) } returns player
        every { entities.indices } returns (0 until 1)
        every { entities.locals } returns intArrayOf(1)
        // When
        task.processLocals(sync, updates, entities, viewport, true)
        // Then
        verifyOrder {
            viewport.setIdle(any())
            task.writeSkip(sync, 0)
        }
    }

    @Test
    fun `Local player not processed in global`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        val viewport = mockk<Viewport>(relaxed = true)
        val entities = mockk<PlayerTrackingSet>(relaxed = true)
        val index = 1

        every { players.indexed(index) } returns player
        every { player.index } returns index
        every { entities.local(index) } returns true
        // When
        task.processGlobals(mockk(relaxed = true), mockk(relaxed = true), entities, viewport, true)
        // Then
        verify(exactly = 0) {
            entities.addSelf(player)
            viewport.setIdle(index)
        }
    }

    @Test
    fun `Global player add`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        val viewport = mockk<Viewport>(relaxed = true)
        val entities = PlayerTrackingSet(100, 100)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val index = 1

        every { player.index } returns index
        every { players.indexed(index) } returns player
        entities.track(player, null)
        every { viewport.lastSeen } returns IntArray(100) { if (it == index) Tile(64, 0).regionPlane.id else 0 }
        every { player.tile } returns value(Tile(81, 14))
        // When
        task.processGlobals(sync, updates, entities, viewport, true)
        // Then
        verifyOrder {
            sync.startBitAccess()
            sync.writeBits(1, true)
            sync.writeBits(2, 0)
            sync.writeBits(1, false)// Encode region
            sync.writeBits(6, 17)
            sync.writeBits(6, 14)
            sync.writeBits(1, true)
            updates.writeBytes(any<ByteArray>())
            task.writeSkip(sync, 2045)
            sync.finishBitAccess()
        }
    }

    @Test
    fun `Skip last global player`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        val viewport = mockk<Viewport>(relaxed = true)
        val entities = mockk<PlayerTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        every { player.index } returns MAX_PLAYERS - 2
        every { players.indexed(MAX_PLAYERS - 2) } returns player
        every { entities.add(MAX_PLAYERS - 2) } returns true
        every { viewport.lastSeen } returns IntArray(MAX_PLAYERS)
        // When
        task.processGlobals(sync, updates, entities, viewport, true)
        // Then
        verifyOrder {
            task.writeSkip(sync, 2044)
            task.writeSkip(sync, 0)
        }
    }

    @TestFactory
    fun `Write skip`() = intArrayOf(0, 30, 250, 2040).mapIndexed { index, skip ->
        dynamicTest("Write skip $skip") {
            // Given
            val writer: Writer = mockk(relaxed = true)
            // When
            task.writeSkip(writer, skip)
            // Then
            verifyOrder {
                writer.writeBits(1, 0)
                if (index > 0) {
                    writer.writeBits(2, index)
                }
                val count = when (index) {
                    1 -> 5
                    2 -> 8
                    3 -> 11
                    else -> return@verifyOrder
                }
                writer.writeBits(count, skip)
            }
        }
    }

    @TestFactory
    fun `Encode region`() = arrayOf(
        RegionChange.Update,
        RegionChange.Height,
        RegionChange.Local,
        RegionChange.Global
    ).mapIndexed { index, updateType ->
        dynamicTest("Encode region change $updateType") {
            // Given
            val writer: Writer = mockk(relaxed = true)
            val value = 10
            val player: Player = mockk(relaxed = true)
            val viewport: Viewport = mockk(relaxed = true)
            every { player.viewport } returns viewport
            every { player.tile } returns value(Tile(0))
            every { viewport.lastSeen } returns IntArray(1)
            every { task.calculateRegionUpdate(anyValue()) } returns updateType
            every { task.calculateRegionValue(any(), anyValue()) } returns value
            // When
            task.encodeRegion(writer, viewport, player)
            // Then
            verifyOrder {
                writer.writeBits(1, updateType != RegionChange.Update)
                if (updateType != RegionChange.Update) {
                    writer.writeBits(2, updateType.id)
                    val count = when (index) {
                        1 -> 2
                        2 -> 5
                        3 -> 18
                        else -> return@verifyOrder
                    }
                    writer.writeBits(count, value)
                }
            }
        }
    }

    @TestFactory
    fun `Region update types`() = arrayOf(
        RegionChange.Update to Delta(0, 0, 0),
        RegionChange.Height to Delta(0, 0, 1),
        RegionChange.Local to Delta(1, 1, 0),
        RegionChange.Local to Delta(0, 1, 0),
        RegionChange.Local to Delta(1, 0, 0),
        RegionChange.Local to Delta(-1, -1, 2),
        RegionChange.Local to Delta(0, -1, 0),
        RegionChange.Local to Delta(-1, 0, 0),
        RegionChange.Global to Delta(2, 2, 3),
        RegionChange.Global to Delta(0, 2, 0),
        RegionChange.Global to Delta(2, 0, 0),
        RegionChange.Global to Delta(-2, -2, 0),
        RegionChange.Global to Delta(0, -2, 0),
        RegionChange.Global to Delta(-2, 0, 0)
    ).map { (expected, delta) ->
        dynamicTest("Region update for movement $delta") {
            // When
            val result = task.calculateRegionUpdate(delta)
            // Then
            assertEquals(expected, result)
        }
    }

    @TestFactory
    fun `Region update values`() = arrayOf(
        Triple(RegionChange.Update, Delta(0, 0, 0), -1),
        Triple(RegionChange.Height, Delta(0, 0, 1), 1),
        Triple(RegionChange.Height, Delta(0, 0, -3), -3),
        Triple(RegionChange.Local, Delta(-1, 1, 0), 5),
        Triple(RegionChange.Local, Delta(0, 1, 1), 14),
        Triple(RegionChange.Local, Delta(1, 1, 2), 23),
        Triple(RegionChange.Local, Delta(1, 0, 3), 28),
        Triple(RegionChange.Local, Delta(1, -1, 0), 2),
        Triple(RegionChange.Local, Delta(0, -1, 1), 9),
        Triple(RegionChange.Local, Delta(-1, -1, 2), 16),
        Triple(RegionChange.Local, Delta(-1, 0, 3), 27),
        Triple(RegionChange.Global, Delta(2, 2, 0), 514),
        Triple(RegionChange.Global, Delta(-2, -2, 1), 130814),
        Triple(RegionChange.Global, Delta(12, -16, 2), 134384)
    ).map { (updateType, delta, expected) ->
        dynamicTest("Region value for movement $delta") {
            // When
            val result = task.calculateRegionValue(updateType, delta)
            // Then
            assertEquals(expected, result)
        }
    }
}