package world.gregs.voidps.engine.client.update.player

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.dsl.module
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.client.update.view.PlayerTrackingSet
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.value
import world.gregs.voidps.network.client.Client
import world.gregs.voidps.network.login.protocol.encode.updatePlayers
import world.gregs.voidps.network.login.protocol.visual.PlayerVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.update.Face
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Tile

internal class PlayerUpdateTaskTest : KoinMock() {

    private lateinit var task: PlayerUpdateTask
    private lateinit var players: Players
    override val modules = listOf(
        module {
            single { Players() }
        },
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
        mockkStatic("world.gregs.voidps.network.login.protocol.encode.PlayerUpdateEncoderKt")
        val client: Client = mockk(relaxed = true)
        every { player.client } returns client
        every { client.updatePlayers(any(), any()) } just Runs
        every { task.processLocals(player, any(), any(), any(), any(), any()) } just Runs
        every { task.processGlobals(player, any(), any(), any(), any(), any()) } just Runs
        // When
        task.run(player)
        // Then
        verifyOrder {
            task.processLocals(player, any(), any(), entities, viewport, true)
            task.processLocals(player, any(), any(), entities, viewport, false)
            task.processGlobals(player, any(), any(), entities, viewport, true)
            task.processGlobals(player, any(), any(), entities, viewport, false)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Local idle player ignored on update`(active: Boolean) {
        // Given
        val activePlayer = mockk<Player>(relaxed = true)
        val idlePlayer = mockk<Player>(relaxed = true)
        val entities = mockk<PlayerTrackingSet>(relaxed = true)
        val viewport = Viewport()
        val idleIndex = 1
        val activeIndex = 2

        every { players.indexed(1) } returns idlePlayer
        every { players.indexed(2) } returns activePlayer
        every { entities.localCount } returns 2
        every { entities.locals } returns intArrayOf(1, 2)
        every { activePlayer.index } returns activeIndex
        every { idlePlayer.index } returns idleIndex
        viewport.setIdle(idleIndex)
        viewport.shift()
        // When
        task.processLocals(idlePlayer, mockk(relaxed = true), mockk(relaxed = true), entities, viewport, active)
        // Then
        verify(exactly = (!active).toInt()) {
            players.indexed(idleIndex)
        }
        verify(exactly = active.toInt()) {
            players.indexed(activeIndex)
        }
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
        every { entities.localCount } returns 1
        every { entities.locals } returns intArrayOf(1)
        every { player.client!!.disconnected } returns true
        every { viewport.lastSeen(player) } returns value(Tile.EMPTY)
        // When
        task.processLocals(player, sync, mockk(relaxed = true), entities, viewport, true)
        // Then
        verify(exactly = 0) { viewport.setIdle(any()) }
        verifyOrder {
            sync.startBitAccess()
            sync.writeBits(1, true)
            sync.writeBits(1, false) // Even when update isn't null (relaxed)
            sync.writeBits(2, 0)
            task.encodeRegion(sync, viewport, player)
            sync.stopBitAccess()
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `Local player move on foot`(run: Boolean) {
        // Given
        val player = mockk<Player>(relaxed = true)
        val viewport = mockk<Viewport>(relaxed = true)
        val entities = mockk<PlayerTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)

        every { player.index } returns 1
        every { players.indexed(1) } returns player
        every { player.visuals.walkStep } returns 0 // North
        every { entities.localCount } returns 1
        every { entities.locals } returns intArrayOf(1)
        every { viewport.delta(player) } returns value(Delta(0, if (run) -2 else -1))
        // When
        task.processLocals(player, sync, mockk(relaxed = true), entities, viewport, true)
        // Then
        verify(exactly = 0) { viewport.setIdle(any()) }
        verifyOrder {
            sync.writeBits(1, true)
            sync.writeBits(1, false)
            sync.writeBits(2, if (run) 2 else 1)
            sync.writeBits(3 + run.toInt(), if (run) 2 else 1)
        }
    }

    @Test
    fun `Local player teleport`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        val viewport = mockk<Viewport>(relaxed = true)
        val entities = mockk<PlayerTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)

        every { player.index } returns 1
        every { player.visuals.flag } returns 2
        every { player.visuals.walkStep } returns -1 // None
        every { player.visuals.runStep } returns -1 // None
        every { players.indexed(1) } returns player
        every { entities.localCount } returns 1
        every { entities.locals } returns intArrayOf(1)
        every { viewport.delta(player) } returns value(Delta(0, -1))
        // When
        task.processLocals(player, sync, mockk(relaxed = true), entities, viewport, true)
        // Then
        verify(exactly = 0) { viewport.setIdle(any()) }
        verifyOrder {
            sync.writeBits(1, true)
            sync.writeBits(1, true)
            sync.writeBits(2, 3)
            sync.writeBits(1, true)
            sync.writeBits(30, 16383)
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

        every { player.index } returns 1
        every { player.visuals.flag } returns 2
        every { player.visuals.flagged(2) } returns true
        val face = Face(direction = 1)
        every { player.visuals.face } returns face
        every { players.indexed(1) } returns player
        every { entities.localCount } returns 1
        every { entities.locals } returns intArrayOf(1)
        every { viewport.delta(any()) } returns value(Delta.EMPTY)
        // When
        task.processLocals(player, sync, updates, entities, viewport, true)
        // Then
        verify(exactly = 0) { viewport.setIdle(any()) }
        verifyOrder {
            sync.writeBits(1, true)
            sync.writeBits(1, true)
            sync.writeBits(2, 0)
            task.writeFlag(updates, 2)
            updates.writeShort(1)
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
        every { players.indexed(1) } returns skipPlayer
        every { players.indexed(2) } returns player
        every { entities.localCount } returns 2
        every { entities.locals } returns intArrayOf(1, 2)
        every { viewport.delta(any()) } returns value(Delta.EMPTY)
        // When
        task.processLocals(player, sync, updates, entities, viewport, true)
        // Then
        verifyOrder {
            viewport.setIdle(index)
            task.writeSkip(sync, 1)
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

        every { players.indexed(1) } returns player
        every { entities.localCount } returns 1
        every { entities.locals } returns intArrayOf(1)
        every { viewport.delta(any()) } returns value(Delta.EMPTY)
        // When
        task.processLocals(player, sync, updates, entities, viewport, true)
        // Then
        verifyOrder {
            viewport.setIdle(any())
            task.writeSkip(sync, 0)
        }
    }

    @Test
    fun `Global player add`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        val viewport = mockk<Viewport>(relaxed = true)
        val entities = mockk<PlayerTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val index = 1

        every { player.visuals.flagged(2) } returns true
        every { player.index } returns index
        every { players.indexed(index) } returns player
        every { viewport.lastSeen(player) } returns value(Tile(64, 0))
        every { player.tile } returns value(Tile(81, 14))
        every { entities.globalCount } returns 1
        every { entities.globals } returns intArrayOf(1)
        // When
        task.processGlobals(player, sync, updates, entities, viewport, true)
        // Then
        verifyOrder {
            sync.startBitAccess()
            sync.writeBits(1, true)
            sync.writeBits(2, 0)
            sync.writeBits(1, false)
            sync.writeBits(6, 17)
            sync.writeBits(6, 14)
            sync.writeBits(1, false)
            sync.stopBitAccess()
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
        Delta(0, 0),
        Delta(0, 0, 1),
        Delta(64, 64),
        Delta(128, 0),
    ).mapIndexed { index, updateType ->
        dynamicTest("Encode region change $updateType") {
            // Given
            val writer: Writer = mockk(relaxed = true)
            val player: Player = mockk(relaxed = true)
            val viewport: Viewport = mockk(relaxed = true)
            every { player.viewport } returns viewport
            every { player.tile } returns value(Tile(0))
            every { viewport.lastSeen(player) } returns value(Tile.EMPTY.add(updateType))
            // When
            task.encodeRegion(writer, viewport, player)
            // Then
            verifyOrder {
                writer.writeBits(1, index != 0)
                if (index != 0) {
                    writer.writeBits(2, index)
                    val count = when (index) {
                        1 -> 2
                        2 -> 5
                        3 -> 18
                        else -> return@verifyOrder
                    }
                    val value = when (index) {
                        1 -> -1
                        2 -> 0
                        3 -> 65024
                        else -> return@verifyOrder
                    }
                    writer.writeBits(count, value)
                }
            }
        }
    }

    @TestFactory
    fun `Region update values`() = arrayOf(
        Triple(PlayerUpdateTask.RegionChange.None, Delta(0, 0, 0), -1),
        Triple(PlayerUpdateTask.RegionChange.Height, Delta(0, 0, 1), 1),
        Triple(PlayerUpdateTask.RegionChange.Height, Delta(0, 0, -3), -3),
        Triple(PlayerUpdateTask.RegionChange.Local, Delta(-1, 1, 0), 5),
        Triple(PlayerUpdateTask.RegionChange.Local, Delta(0, 1, 1), 14),
        Triple(PlayerUpdateTask.RegionChange.Local, Delta(1, 1, 2), 23),
        Triple(PlayerUpdateTask.RegionChange.Local, Delta(1, 0, 3), 28),
        Triple(PlayerUpdateTask.RegionChange.Local, Delta(1, -1, 0), 2),
        Triple(PlayerUpdateTask.RegionChange.Local, Delta(0, -1, 1), 9),
        Triple(PlayerUpdateTask.RegionChange.Local, Delta(-1, -1, 2), 16),
        Triple(PlayerUpdateTask.RegionChange.Local, Delta(-1, 0, 3), 27),
        Triple(PlayerUpdateTask.RegionChange.Global, Delta(2, 2, 0), 514),
        Triple(PlayerUpdateTask.RegionChange.Global, Delta(-2, -2, 1), 130814),
        Triple(PlayerUpdateTask.RegionChange.Global, Delta(12, -16, 2), 134384),
    ).map { (type, delta, _) ->
        dynamicTest("Region value for movement $delta") {
            // When
            val result = task.calculateRegionUpdate(delta)
            // Then
            assertEquals(type, result)
        }
    }

    @Test
    fun `Write small flag`() {
        // Given
        val writer = ArrayWriter()
        // When
        task.writeFlag(writer, 0x10)
        // Then
        val reader = ArrayReader(writer.array())
        assertEquals(0x10, reader.readByte())
    }

    @Test
    fun `Write medium flag`() {
        // Given
        val writer = ArrayWriter()
        // When
        task.writeFlag(writer, 0x100)
        // Then
        val reader = ArrayReader(writer.array())
        assertEquals(0x40, reader.readUnsignedByte())
        assertEquals(0x1, reader.readUnsignedByte())
    }

    @Test
    fun `Write large flag`() {
        // Given
        val writer = ArrayWriter()
        // When
        task.writeFlag(writer, 0x10000)
        // Then
        val reader = ArrayReader(writer.array())
        assertEquals(0x40, reader.readUnsignedByte())
        assertEquals(0x40, reader.readUnsignedByte())
        assertEquals(0x1, reader.readUnsignedByte())
    }
}
