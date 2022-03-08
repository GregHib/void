package world.gregs.voidps.engine.client.update.task

import io.mockk.*
import it.unimi.dsi.fastutil.ints.IntArrayList
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.update.task.npc.NPCUpdateTask
import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.value
import world.gregs.voidps.network.visual.NPCVisuals
import world.gregs.voidps.network.visual.VisualEncoder


internal class NPCUpdateTaskTest : KoinMock() {

    lateinit var task: NPCUpdateTask
    lateinit var npcs: NPCs
    lateinit var player: Player
    override val modules = listOf(
        eventModule,
        entityListModule
    )
    private lateinit var encoder: VisualEncoder<NPCVisuals>

    @BeforeEach
    fun setup() {
        player = mockk(relaxed = true)
        npcs = mockk(relaxed = true)
        encoder = mockk(relaxed = true)
        every { encoder.initial } returns true
        every { encoder.mask } returns 2
        task = spyk(NPCUpdateTask(npcs, listOf(encoder)))
    }

    @Test
    fun `Local npc removed`() {
        // Given
        val entities = IntArrayList.of(1)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        every { npcs.indexed(1) } returns null
        // When
        task.processLocals(player, sync, updates, entities)
        // Then
        verifyOrder {
            sync.writeBits(8, 1)
            sync.writeBits(1, true)
            sync.writeBits(2, 3)
        }
        verify(exactly = 0) {
            task.writeFlag(updates, any())
            encoder.encode(updates, any())
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
        val entities = IntArrayList.of(npc.index)
        every { npc.def } returns NPCDefinition(extras = mapOf("crawl" to false))
        every { npcs.indexed(1) } returns npc
        every { npc.movement.delta } returns Delta(0, 1)
        every { npc.movement.walkStep } returns Direction.NORTH
        every { npc.movement.runStep } returns Direction.NONE
        every { npc.visuals.flag } returns if (update) 2 else 0
        // When
        task.processLocals(player, sync, updates, entities)
        // Then
        verifyOrder {
            sync.writeBits(8, 1)
            sync.writeBits(1, true)
            sync.writeBits(2, 1)
            sync.writeBits(3, 0)
            sync.writeBits(1, update)
            if (update) {
                task.writeFlag(updates, 2)
                encoder.encode(updates, any())
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
        val entities = IntArrayList.of(npc.index)
        every { npc.def } returns NPCDefinition(extras = mapOf("crawl" to true))
        every { npcs.indexed(1) } returns npc
        every { npc.movement.delta } returns Delta(0, 1)
        every { npc.movement.walkStep } returns Direction.NORTH
        every { npc.visuals.flag } returns if (update) 2 else 0
        // When
        task.processLocals(player, sync, updates, entities)
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
                encoder.encode(updates, any())
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
        val entities = IntArrayList.of(npc.index)
        every { npc.def } returns NPCDefinition(extras = mapOf("crawl" to false))
        every { npcs.indexed(1) } returns npc
        every { npc.movement.delta } returns Delta(0, 1)
        every { npc.movement.walkStep } returns Direction.NORTH
        every { npc.movement.runStep } returns Direction.NORTH
        every { npc.visuals.flag } returns if (update) 2 else 0
        // When
        task.processLocals(player, sync, updates, entities)
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
                encoder.encode(updates, any())
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Addition of a new npc`(update: Boolean) {
        // Given
        val entities = mockk<IntArrayList>(relaxed = true)
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
        every { npc.visuals.turn } returns mockk(relaxed = true)
        every { npc.visuals.turn.direction } returns direction
        every { npcs.getDirect(player.tile.regionPlane) } returns listOf(index)
        every { npcs.indexed(index) } returns npc
        every { npc.visuals.flag } returns if (update) 2 else 0
        every { npc.visuals.flagged(2) } returns update
        // When
        task.processAdditions(player, sync, updates, entities)
        // Then
        verifyOrder {
            sync.writeBits(15, index)
            sync.writeBits(2, 0)
            sync.writeBits(1, false)
            sync.writeBits(5, 35)
            sync.writeBits(5, 37)
            sync.writeBits(3, 2)
            sync.writeBits(1, update)
            sync.writeBits(14, id)
            if (update) {
                task.writeFlag(updates, 2)
                encoder.encode(updates, any())
            }
            sync.writeBits(15, -1)
        }
    }

    @Test
    fun `Don't add if locals over cap`() {
        // Given
        val entities = mockk<IntArrayList>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        val index = 1
        val id = 20
        every { player.tile } returns value(Tile(0, 0))
        every { npc.tile } returns value(Tile(5, 3, 0))
        every { npc.index } returns index
        every { npc.def.id } returns id
        every { npcs.getDirect(player.tile.regionPlane) } returns listOf(index)
        every { npcs.indexed(index) } returns npc
        every { npc.visuals.turn.direction } returns 8194
        every { entities.size } returns 256
        // When
        task.processAdditions(player, sync, updates, entities)
        // Then
        verifyOrder {
            sync.writeBits(15, -1)
        }
    }

    @Test
    fun `Skip if not within view`() {
        // Given
        val entities = IntArrayList()
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        val index = 1
        every { player.tile } returns value(Tile(0, 0))
        every { npc.tile } returns value(Tile(15, 15, 0))
        every { npc.index } returns index
        every { npc.def.id } returns 20
        every { npcs.getDirect(player.tile.regionPlane) } returns listOf(index)
        every { npcs.indexed(index) } returns npc
        every { npc.visuals.turn.direction } returns 8194
        // When
        task.processAdditions(player, sync, updates, entities)
        // Then
        verifyOrder {
            sync.writeBits(15, -1)
        }
    }

    @Test
    fun `Skip if already local`() {
        // Given
        val entities = IntArrayList.of(1)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        val index = 1
        every { player.tile } returns value(Tile(0, 0))
        every { npc.tile } returns value(Tile(5, 3, 0))
        every { npc.index } returns index
        every { npc.def.id } returns 20
        every { npcs.getDirect(player.tile.regionPlane) } returns listOf(index)
        every { npcs.indexed(index) } returns npc
        every { npc.visuals.turn.direction } returns 8194
        // When
        task.processAdditions(player, sync, updates, entities)
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
        val reader = BufferReader(writer.array())
        Assertions.assertEquals(0x10, reader.readByte())
    }

    @Test
    fun `Write large flag`() {
        // Given
        val writer = BufferWriter()
        // When
        task.writeFlag(writer, 0x100)
        // Then
        val reader = BufferReader(writer.array())
        Assertions.assertEquals(0x10, reader.readUnsignedByte())
        Assertions.assertEquals(0x1, reader.readUnsignedByte())
    }
}
