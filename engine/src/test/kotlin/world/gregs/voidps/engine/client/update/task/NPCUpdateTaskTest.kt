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
import world.gregs.voidps.engine.client.update.task.npc.NPCUpdateTask
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCTrackingSet
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.value
import world.gregs.voidps.network.Client
import world.gregs.voidps.network.encode.updateNPCs
import world.gregs.voidps.network.visual.NPCVisuals
import world.gregs.voidps.network.visual.VisualEncoder

internal class NPCUpdateTaskTest : KoinMock() {

    lateinit var task: NPCUpdateTask
    lateinit var npcs: NPCs
    override val modules = listOf(
        eventModule,
        entityListModule
    )
    private lateinit var encoder: VisualEncoder<NPCVisuals>

    @BeforeEach
    fun setup() {
        npcs = mockk(relaxed = true)
        encoder = mockk(relaxed = true)
        every { encoder.initial } returns true
        every { encoder.mask } returns 2
        task = spyk(NPCUpdateTask(npcs, listOf(encoder)))
    }

    @Test
    fun `Called for each player with sessions`() {
        // Given
        val player = mockk<Player>(relaxed = true)
        mockkStatic("world.gregs.voidps.network.encode.NPCUpdateEncoderKt")
        val client: Client = mockk(relaxed = true)
        every { player.client } returns client
        every { client.updateNPCs(any(), any()) } just Runs
        every { player.viewport.npcs.addIndices } returns 0..0
        // When
        task.run(player)
        // Then
        verify {
            client.updateNPCs(any(), any())
        }
    }

    @Test
    fun `Local npc removed`() {
        // Given
        val entities = mockk<NPCTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        every { npc.index } returns 1
        every { npcs.indexed(1) } returns npc
        every { entities.remove(1) } returns true
        every { entities.locals } returns IntArrayList.of(npc.index)
        every { npc.change } returns LocalChange.Update
        every { npc.visuals } returns mockk(relaxed = true)
        // When
        task.processLocals(sync, updates, entities)
        // Then
        verifyOrder {
            sync.startBitAccess()
            sync.writeBits(8, 1)
            sync.writeBits(1, true)
            sync.writeBits(2, 3)
            sync.finishBitAccess()
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
        val entities = mockk<NPCTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        every { npc.index } returns 1
        every { entities.locals } returns IntArrayList.of(npc.index)
        every { npcs.indexed(1) } returns npc
        every { npc.change } returns LocalChange.Walk
        val direction = 4
        every { npc.walkDirection } returns direction
        every { npc.visuals.flag } returns if (update) 2 else 0
//        every { npc.visuals.aspects[2] } returns if (update) mockk() else null
        every { npc.visuals.flagged(2) } returns update
        // When
        task.processLocals(sync, updates, entities)
        // Then
        verifyOrder {
            sync.startBitAccess()
            sync.writeBits(8, 1)
            sync.writeBits(1, true)
            sync.writeBits(2, 1)
            sync.writeBits(3, direction)
            sync.writeBits(1, update)
            if (update) {
                task.writeFlag(updates, 2)
                encoder.encode(updates, any())
            }
            sync.finishBitAccess()
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Local npc crawl and update`(update: Boolean) {
        // Given
        val entities = mockk<NPCTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        every { npc.index } returns 1
        every { entities.locals } returns IntArrayList.of(npc.index)
        every { npcs.indexed(1) } returns npc
        every { npc.change } returns LocalChange.Crawl
        val direction = 4
        every { npc.walkDirection } returns direction
        every { npc.visuals.flag } returns if (update) 2 else 0
//        every { npc.visuals.aspects[2] } returns if (update) mockk() else null
        every { npc.visuals.flagged(2) } returns update
        // When
        task.processLocals(sync, updates, entities)
        // Then
        verifyOrder {
            sync.startBitAccess()
            sync.writeBits(8, 1)
            sync.writeBits(1, true)
            sync.writeBits(2, 2)
            sync.writeBits(1, false)
            sync.writeBits(3, direction)
            sync.writeBits(1, update)
            if (update) {
                task.writeFlag(updates, 2)
                encoder.encode(updates, any())
            }
            sync.finishBitAccess()
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Local npc run and update`(update: Boolean) {
        // Given
        val entities = mockk<NPCTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        every { npc.index } returns 1
        every { npcs.indexed(1) } returns npc
        every { entities.locals } returns IntArrayList.of(npc.index)
        every { npc.change } returns LocalChange.Run
        val walkDirection = 4
        val runDirection = 8
        every { npc.walkDirection } returns walkDirection
        every { npc.runDirection } returns runDirection
        every { npc.visuals.flag } returns if (update) 2 else 0
//        every { npc.visuals.aspects[2] } returns if (update) mockk() else null
        every { npc.visuals.flagged(2) } returns update
        // When
        task.processLocals(sync, updates, entities)
        // Then
        verifyOrder {
            sync.startBitAccess()
            sync.writeBits(8, 1)
            sync.writeBits(1, true)
            sync.writeBits(2, 2)
            sync.writeBits(1, true)
            sync.writeBits(3, walkDirection)
            sync.writeBits(3, runDirection)
            sync.writeBits(1, update)
            if (update) {
                task.writeFlag(updates, 2)
                encoder.encode(updates, any())
            }
            sync.finishBitAccess()
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `NPC Additions`(update: Boolean) {
        // Given
        mockkStatic("world.gregs.voidps.engine.entity.character.npc.NPCVisualExtensionsKt")
        val client: Player = mockk(relaxed = true)
        val entities = mockk<NPCTrackingSet>(relaxed = true)
        val sync: Writer = mockk(relaxed = true)
        val updates: Writer = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        val index = 1
        val direction = 12345
        val id = 20
        every { client.tile } returns value(Tile(0, 0))
        every { npc.tile } returns value(Tile(5, 3, 1))
        every { npc.index } returns index
        every { npc.def.id } returns id
        every { npc.visuals.turn } returns mockk(relaxed = true)
        every { npc.visuals.turn.direction } returns direction
        every { entities.add } returns IntArray(1) { npc.index }
        every { npcs.indexed(index) } returns npc
        every { entities.addCount } returns 1
        every { npc.visuals.flag } returns if (update) 2 else 0
//        every { npc.visuals.aspects[2] } returns if (update) mockk() else null
        every { npc.visuals.flagged(2) } returns update
        // When
        task.processAdditions(sync, updates, client, entities)
        // Then
        verifyOrder {
            sync.writeBits(15, index)
            sync.writeBits(2, 1)
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
            sync.finishBitAccess()
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