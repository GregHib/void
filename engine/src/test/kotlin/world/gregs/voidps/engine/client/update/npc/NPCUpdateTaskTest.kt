package world.gregs.voidps.engine.client.update.npc

import io.mockk.*
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.dsl.module
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.update.view.Viewport
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.value
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.network.login.protocol.visual.VisualEncoder
import world.gregs.voidps.network.login.protocol.visual.update.Animation
import world.gregs.voidps.network.login.protocol.visual.update.Face
import world.gregs.voidps.type.Tile

internal class NPCUpdateTaskTest : KoinMock() {

    private lateinit var task: NPCUpdateTask
    private lateinit var npcs: NPCs
    private lateinit var player: Player
    private lateinit var viewport: Viewport
    override val modules = listOf(
        module {
            single { NPCs(get(), get(), get(), AreaDefinitions()) }
        },
    )
    private lateinit var encoder: VisualEncoder<NPCVisuals>
    private lateinit var initialEncoder: VisualEncoder<NPCVisuals>

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        viewport = mockk(relaxed = true)
        every { viewport.radius } returns 15
        npcs = mockk(relaxed = true)
        initialEncoder = mockk(relaxed = true)
        every { initialEncoder.initial } returns true
        every { initialEncoder.mask } returns 2
        encoder = mockk(relaxed = true)
        every { encoder.initial } returns false
        every { encoder.mask } returns 8
        task = spyk(NPCUpdateTask(npcs, listOf(initialEncoder, encoder)))
    }

    @Test
    fun `Local npc removed`() {
        // Given
        val entities = IntOpenHashSet.of(1)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        every { npcs.indexed(1) } returns null
        // When
        task.processLocals(player, viewport, sync, updates, entities)
        // Then
        verifyOrder {
            sync.writeBits(8, 1)
            sync.writeBits(1, true)
            sync.writeBits(2, 3)
        }
        verify(exactly = 0) {
            task.writeFlag(updates, any())
            initialEncoder.encode(updates, any(), any())
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Local npc walk and update`(update: Boolean) {
        // Given
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        every { npc.index } returns 1
        val entities = IntOpenHashSet.of(npc.index)
        every { npc.def } returns NPCDefinition(extras = mapOf("crawl" to false))
        every { npcs.indexed(1) } returns npc
        every { npc.visuals.moved } returns true
        every { npc.visuals.walkStep } returns 0 // North
        every { npc.visuals.runStep } returns -1 // None
        every { npc.visuals.flag } returns if (update) 2 else 0
        // When
        task.processLocals(player, viewport, sync, updates, entities)
        // Then
        verifyOrder {
            sync.writeBits(8, 1)
            sync.writeBits(1, true)
            sync.writeBits(2, 1)
            sync.writeBits(3, 0)
            sync.writeBits(1, update)
            if (update) {
                task.writeFlag(updates, 2)
                initialEncoder.encode(updates, any(), any())
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Local npc crawl and update`(update: Boolean) {
        // Given
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        every { npc.index } returns 1
        val entities = IntOpenHashSet.of(npc.index)
        every { npc.def } returns NPCDefinition(extras = mapOf("crawl" to true))
        every { npcs.indexed(1) } returns npc
        every { npc.visuals.moved } returns true
        every { npc.visuals.walkStep } returns 0 // North
        every { npc.visuals.flag } returns if (update) 2 else 0
        // When
        task.processLocals(player, viewport, sync, updates, entities)
        // Then
        verifyOrder {
            sync.writeBits(8, 1)
            sync.writeBits(1, true)
            sync.writeBits(2, 2)
            sync.writeBits(1, false)
            sync.writeBits(3, 0)
            sync.writeBits(1, update)
            if (update) {
                task.writeFlag(updates, 2)
                initialEncoder.encode(updates, any(), any())
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Local npc run and update`(update: Boolean) {
        // Given
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        every { npc.index } returns 1
        val entities = IntOpenHashSet.of(npc.index)
        every { npc.def } returns NPCDefinition(extras = mapOf("crawl" to false))
        every { npcs.indexed(1) } returns npc
        every { npc.visuals.moved } returns true
        every { npc.visuals.walkStep } returns 0 // North
        every { npc.visuals.runStep } returns 0 // North
        every { npc.visuals.flag } returns if (update) 2 else 0
        // When
        task.processLocals(player, viewport, sync, updates, entities)
        // Then
        verifyOrder {
            sync.writeBits(8, 1)
            sync.writeBits(1, true)
            sync.writeBits(2, 2)
            sync.writeBits(1, true)
            sync.writeBits(3, 0)
            sync.writeBits(3, 0)
            sync.writeBits(1, update)
            if (update) {
                task.writeFlag(updates, 2)
                initialEncoder.encode(updates, any(), any())
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Addition of a new npc`(update: Boolean) {
        // Given
        val entities = mockk<IntOpenHashSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        val index = 1
        val direction = 12345
        val id = 20
        every { player.tile } returns value(Tile(0, 0))
        every { npc.tile } returns value(Tile(5, 3, 0))
        every { npc.index } returns index
        every { npc.def.id } returns id
        every { npc.visuals.face } returns Face(direction = direction)
        every { npc.visuals.animation } returns Animation(123)
        every { npcs.getDirect(player.tile.regionLevel) } returns listOf(index)
        every { npcs.indexed(index) } returns npc
        every { npc.visuals.flag } returns if (update) 10 else 0
        every { npc.visuals.flagged(2) } returns update
        every { npc.visuals.flagged(8) } returns update
        // When
        task.processAdditions(player, viewport, sync, updates, entities)
        // Then
        verifyOrder {
            sync.writeBits(15, index)
            sync.writeBits(2, 0)
            sync.writeBits(1, false)
            sync.writeBits(5, 35)
            sync.writeBits(5, 37)
            sync.writeBits(3, 2)
            sync.writeBits(1, true)
            sync.writeBits(14, id)
            task.writeFlag(updates, if (update) 10 else 2)
            initialEncoder.encode(updates, any(), any())
            if (update) {
                encoder.encode(updates, any(), any())
            }
            sync.writeBits(15, -1)
        }
    }

    @Test
    fun `Don't add if locals over cap`() {
        // Given
        val entities = mockk<IntOpenHashSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        val index = 1
        val id = 20
        every { player.tile } returns value(Tile(0, 0))
        every { npc.tile } returns value(Tile(5, 3, 0))
        every { npc.index } returns index
        every { npc.def.id } returns id
        every { npcs.getDirect(player.tile.regionLevel) } returns listOf(index)
        every { npcs.indexed(index) } returns npc
        every { npc.visuals.face.direction } returns 8194
        every { entities.size } returns 256
        // When
        task.processAdditions(player, viewport, sync, updates, entities)
        // Then
        verifyOrder {
            sync.writeBits(15, -1)
        }
    }

    @Test
    fun `Skip if not within view`() {
        // Given
        val entities = IntOpenHashSet()
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        val index = 1
        every { player.tile } returns value(Tile(0, 0))
        every { npc.tile } returns value(Tile(15, 15, 0))
        every { npc.index } returns index
        every { npc.def.id } returns 20
        every { npcs.getDirect(player.tile.regionLevel) } returns listOf(index)
        every { npcs.indexed(index) } returns npc
        every { npc.visuals.face.direction } returns 8194
        // When
        task.processAdditions(player, viewport, sync, updates, entities)
        // Then
        verifyOrder {
            sync.writeBits(15, -1)
        }
    }

    @Test
    fun `Skip if already local`() {
        // Given
        val entities = IntOpenHashSet.of(1)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        val index = 1
        every { player.tile } returns value(Tile(0, 0))
        every { npc.tile } returns value(Tile(5, 3, 0))
        every { npc.index } returns index
        every { npc.def.id } returns 20
        every { npcs.getDirect(player.tile.regionLevel) } returns listOf(index)
        every { npcs.indexed(index) } returns npc
        every { npc.visuals.face.direction } returns 8194
        // When
        task.processAdditions(player, viewport, sync, updates, entities)
        // Then
        verifyOrder {
            sync.writeBits(15, -1)
        }
    }

    @Test
    fun `Write small flag`() {
        // Given
        val writer = BufferWriter()
        // When
        task.writeFlag(writer, 0x10)
        // Then
        val reader = ArrayReader(writer.array())
        Assertions.assertEquals(0x10, reader.readByte())
    }

    @Test
    fun `Write large flag`() {
        // Given
        val writer = BufferWriter()
        // When
        task.writeFlag(writer, 0x100)
        // Then
        val reader = ArrayReader(writer.array())
        Assertions.assertEquals(0x10, reader.readUnsignedByte())
        Assertions.assertEquals(0x1, reader.readUnsignedByte())
    }
}
