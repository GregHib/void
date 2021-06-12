package world.gregs.voidps.engine.client.update.task

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.engine.client.update.task.npc.NPCVisualsTask
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.list.PooledMapList
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.utility.get

internal class NPCVisualsTaskTest : KoinMock() {

    private val encoder: VisualEncoder<Visual> = mockk(relaxed = true)

    init {
        every { encoder.mask } returns 0x8
    }

    private val addMasks = intArrayOf(encoder.mask)
    private val npcs: PooledMapList<NPC> = mockk(relaxed = true)
    private val encoderModule = module {
        single { spyk(
            NPCVisualsTask(
                npcs,
                arrayOf(encoder),
                addMasks
            )
        ) }
    }
    override val modules = listOf(eventModule, entityListModule, encoderModule)

    @Test
    fun `Run runs all in parallel`() {
        // Given
        val updateTask: NPCVisualsTask = get()
        val npc: NPC = mockk(relaxed = true)
        every { npcs.forEach(any()) } answers {
            arg<(Character) -> Unit>(0).invoke(npc)
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
    fun `Update writes addition if any addMasks changed`() {
        // Given
        val task: NPCVisualsTask = get()
        val visuals: Visuals = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        every { npc.visuals } returns visuals
        every { visuals.flag } returns 1
        every { visuals.flagged(any()) } returns true
        every { task.encodeUpdate(visuals) } just Runs
        every { task.encodeAddition(visuals) } just Runs
        // When
        task.runAsync(npc)
        // Then
        verifyOrder {
            task.encodeUpdate(visuals)
            task.encodeAddition(visuals)
            visuals.flag = 0
        }
    }

    @Test
    fun `Update doesn't rewrite addition`() {
        // Given
        val task: NPCVisualsTask = get()
        val visuals: Visuals = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        every { npc.visuals } returns visuals
        every { visuals.flag } returns 1
        every { visuals.flagged(any()) } returns false
        // When
        task.runAsync(npc)
        // Then
        verify(exactly = 0) { task.encodeAddition(visuals) }
        verifyOrder {
            task.encodeUpdate(visuals)
            visuals.flag = 0
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
        val map: MutableMap<Int, Visual> = mockk(relaxed = true)
        every { visuals.aspects } returns map
        every { map[any()] } returns mockk(relaxed = true)
        // When
        updateTask.encodeUpdate(visuals)
        // Then
        verifyOrder {
            updateTask.writeFlag(any(), 0x8)
            encoder.encodeVisual(any(), any())
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
    fun `Encode addition`() {
        // Given
        val updateTask: NPCVisualsTask = get()
        val visuals: Visuals = mockk(relaxed = true)
        val map: MutableMap<Int, Visual> = mockk(relaxed = true)
        every { visuals.aspects } returns map
        every { map[any()] } returns mockk(relaxed = true)
        // When
        updateTask.encodeAddition(visuals)
        // Then
        verifyOrder {
            updateTask.writeFlag(any(), 0)
            encoder.encodeVisual(any(), any())
            visuals.addition = any()
        }
    }

    @Test
    fun `Encode addition update`() {
        // Given
        val updateTask: NPCVisualsTask = get()
        val visuals: Visuals = mockk(relaxed = true)
        val map: MutableMap<Int, Visual> = mockk(relaxed = true)
        every { visuals.aspects } returns map
        every { map[any()] } returns mockk(relaxed = true)
        every { visuals.flagged(any()) } returns true
        // When
        updateTask.encodeAddition(visuals)
        // Then
        verifyOrder {
            updateTask.writeFlag(any(), addMasks.sum())
            encoder.encodeVisual(any(), any())
            visuals.addition = any()
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
        val reader = BufferReader(writer.array())
        assertEquals(0x10, reader.readByte())
    }

    @Test
    fun `Write large flag`() {
        // Given
        val updateTask: NPCVisualsTask = get()
        val writer = BufferWriter()
        // When
        updateTask.writeFlag(writer, 0x100)
        // Then
        val reader = BufferReader(writer.array())
        assertEquals(0x10, reader.readUnsignedByte())
        assertEquals(0x1, reader.readUnsignedByte())
    }
}