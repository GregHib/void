package rs.dusk.engine.client.update.task

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import rs.dusk.core.io.read.BufferReader
import rs.dusk.core.io.write.BufferWriter
import rs.dusk.engine.model.entity.index.Indexed
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.npc.NPCs
import rs.dusk.engine.model.entity.index.update.Visual
import rs.dusk.engine.model.entity.index.update.VisualEncoder
import rs.dusk.engine.model.entity.index.update.Visuals
import rs.dusk.engine.model.entity.list.PooledMapList
import rs.dusk.engine.model.entity.list.entityListModule
import rs.dusk.engine.script.KoinMock
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 26, 2020
 */
internal class NPCVisualsTaskTest : KoinMock() {

    private val encoder: VisualEncoder<Visual> = mockk(relaxed = true)

    init {
        every { encoder.mask } returns 0x8
    }

    private val npcs: PooledMapList<NPC> = mockk(relaxed = true)
    private val encoderModule = module {
        single { spyk(NPCVisualsTask(npcs, arrayOf(encoder))) }
    }
    override val modules = listOf(entityListModule, encoderModule)

    @Test
    fun `Run runs all in parallel`() {
        // Given
        val updateTask: NPCVisualsTask = get()
        val npc: NPC = mockk(relaxed = true)
        every { npcs.forEach(any()) } answers {
            arg<(Indexed) -> Unit>(0).invoke(npc)
        }
        val visuals: Visuals = mockk(relaxed = true)
        every { npc.visuals } returns visuals
        // When
        updateTask.run()
        // Then
        verify {
            updateTask.runAsync(npc)
        }
    }

    @Test
    fun `Update skips if un-flagged`() {
        // Given
        val task: NPCVisualsTask = get()
        val npcs: NPCs = get()
        val visuals: Visuals = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        every { npc.visuals } returns visuals
        npcs.add(0, npc)
        // When
        every { visuals.flag } returns 0
        task.runAsync(npc)
        // Then
        verify { visuals.update = null }
        verify(exactly = 0) {
            task.encodeUpdate(any())
        }
    }

    @Test
    fun `Encode flagged update`() {
        // Given
        val updateTask: NPCVisualsTask = get()
        val visuals: Visuals = mockk(relaxed = true)
        val mask = 0x8
        every { visuals.flag } returns mask
        every { visuals.flagged(mask) } returns true
        every { visuals.aspects[any()] } returns mockk(relaxed = true)
        // When
        updateTask.encodeUpdate(visuals)
        // Then
        verifyOrder {
            updateTask.writeFlag(any(), 0x8)
            encoder.encode(any(), any())
            visuals.update = any()
        }
    }

    @Test
    fun `Encode ignores not flagged update`() {
        // Given
        val updateTask: NPCVisualsTask = get()
        val visuals: Visuals = mockk(relaxed = true)
        val mask = 0x8
        every { visuals.flag } returns mask
        every { visuals.flagged(mask) } returns false

        // When
        updateTask.encodeUpdate(visuals)
        // Then
        verify {
            updateTask.writeFlag(any(), 0x8)
            visuals.update = any()
        }
        verify(exactly = 0) {
            encoder.encode(any(), any())
        }
    }

    @Test
    fun `Write small flag`() {
        // Given
        val updateTask: NPCVisualsTask = get()
        val writer = BufferWriter()
        // When
        updateTask.writeFlag(writer, 0x10)
        // Then
        val reader = BufferReader(writer.buffer.array())
        assertEquals(0x10, reader.readByte())
    }

    @Test
    fun `Write medium flag`() {
        // Given
        val updateTask: NPCVisualsTask = get()
        val writer = BufferWriter()
        // When
        updateTask.writeFlag(writer, 0x100)
        // Then
        val reader = BufferReader(writer.buffer.array())
        assertEquals(0x80, reader.readUnsignedByte())
        assertEquals(0x1, reader.readUnsignedByte())
    }

    @Test
    fun `Write large flag`() {
        // Given
        val updateTask: NPCVisualsTask = get()
        val writer = BufferWriter()
        // When
        updateTask.writeFlag(writer, 0x10000)
        // Then
        val reader = BufferReader(writer.buffer.array())
        assertEquals(0x80, reader.readUnsignedByte())
        assertEquals(0x80, reader.readUnsignedByte())
        assertEquals(0x1, reader.readUnsignedByte())
    }
}