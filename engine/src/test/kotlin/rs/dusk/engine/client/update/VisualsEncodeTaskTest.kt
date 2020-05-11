package rs.dusk.engine.client.update

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import rs.dusk.core.io.read.BufferReader
import rs.dusk.core.io.write.BufferWriter
import rs.dusk.engine.engineModule
import rs.dusk.engine.entity.list.PooledMapList
import rs.dusk.engine.entity.list.entityListModule
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.Indexed
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.Visuals
import rs.dusk.engine.script.KoinMock
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 26, 2020
 */
internal class VisualsEncodeTaskTest : KoinMock() {

    private val encoder: VisualEncoder<Visual> = mockk(relaxed = true)

    init {
        every { encoder.mask } returns 0x8
    }

    private val addMasks = intArrayOf(encoder.mask)
    private val entities: PooledMapList<Player> = mockk(relaxed = true)
    private val encoderModule = module {
        single { spyk(VisualsEncodeTask(entities, arrayOf(encoder), addMasks, 0x800, get())) }
    }
    override val modules = listOf(engineModule, entityListModule, encoderModule)

    @Test
    fun `Run runs all in parallel`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val player: Player = mockk(relaxed = true)
        every { entities.forEach(any()) } answers {
            arg<(Indexed) -> Unit>(0).invoke(player)
        }
        val visuals: Visuals = mockk(relaxed = true)
        every { player.visuals } returns visuals
        // When
        updateTask.run()
        // Then
        verify {
            updateTask.update(visuals)
        }
    }

    @Test
    fun `Update skips if un-flagged`() {
        // Given
        val task: VisualsEncodeTask<Player> = get()
        val players: Players = get()
        val visuals: Visuals = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        every { player.visuals } returns visuals
        players.add(0, player)
        // When
        every { visuals.flag } returns 0
        runBlocking {
            task.update(visuals).await()
        }
        // Then
        verify { visuals.update = null }
        verify(exactly = 0) {
            task.encodeUpdate(any())
            task.encodeAddition(any())
        }
    }

    @Test
    fun `Update writes addition if any addMasks changed`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val visuals: Visuals = mockk(relaxed = true)
        every { visuals.flag } returns 1
        every { visuals.flagged(any()) } returns true
        every { updateTask.encodeUpdate(visuals) } just Runs
        every { updateTask.encodeAddition(visuals) } just Runs
        // When
        runBlocking {
            updateTask.update(visuals).await()
        }
        // Then
        verifyOrder {
            updateTask.encodeUpdate(visuals)
            updateTask.encodeAddition(visuals)
            visuals.flag = 0
        }
    }

    @Test
    fun `Update doesn't rewrite addition`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val visuals: Visuals = mockk(relaxed = true)
        every { visuals.flag } returns 1
        every { visuals.flagged(any()) } returns false
        // When
        runBlocking {
            updateTask.update(visuals).await()
        }
        // Then
        verify(exactly = 0) { updateTask.encodeAddition(visuals) }
        verifyOrder {
            updateTask.encodeUpdate(visuals)
            visuals.flag = 0
        }
    }

    @Test
    fun `Encode flagged update`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val visuals: Visuals = mockk(relaxed = true)
        val mask = 0x8
        every { visuals.flag } returns mask
        every { visuals.flagged(mask) } returns true
        every { visuals.aspects[any()] } returns mockk(relaxed = true)
        // When
        updateTask.encodeUpdate(visuals)
        // Then
        verifyOrder {
            updateTask.writeFlag(any(), 0x8, 0x800)
            encoder.encode(any(), any())
            visuals.update = any()
        }
    }

    @Test
    fun `Encode ignores not flagged update`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val visuals: Visuals = mockk(relaxed = true)
        val mask = 0x8
        every { visuals.flag } returns mask
        every { visuals.flagged(mask) } returns false

        // When
        updateTask.encodeUpdate(visuals)
        // Then
        verify {
            updateTask.writeFlag(any(), 0x8, 0x800)
            visuals.update = any()
        }
        verify(exactly = 0) {
            encoder.encode(any(), any())
        }
    }

    @Test
    fun `Encode addition`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val visuals: Visuals = mockk(relaxed = true)
        every { visuals.aspects[any()] } returns mockk(relaxed = true)
        // When
        updateTask.encodeAddition(visuals)
        // Then
        verifyOrder {
            updateTask.writeFlag(any(), addMasks.sum(), 0x800)
            encoder.encode(any(), any())
            visuals.addition = any()
        }
    }

    @Test
    fun `Write small flag`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val writer = BufferWriter()
        // When
        updateTask.writeFlag(writer, 0x10, 0x800)
        // Then
        val reader = BufferReader(writer.buffer.array())
        assertEquals(0x10, reader.readByte())
    }

    @Test
    fun `Write medium flag`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val writer = BufferWriter()
        // When
        updateTask.writeFlag(writer, 0x100, 0x800)
        // Then
        val reader = BufferReader(writer.buffer.array())
        assertEquals(0x80, reader.readUnsignedByte())
        assertEquals(0x1, reader.readUnsignedByte())
    }

    @Test
    fun `Write large flag`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val writer = BufferWriter()
        // When
        updateTask.writeFlag(writer, 0x10000, 0x800)
        // Then
        val reader = BufferReader(writer.buffer.array())
        assertEquals(0x80, reader.readUnsignedByte())
        assertEquals(0x8, reader.readUnsignedByte())
        assertEquals(0x1, reader.readUnsignedByte())
    }
}