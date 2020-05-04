package rs.dusk.engine.client.update

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
    private val addMasks = intArrayOf(0x8)
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
        val updateTask: VisualsEncodeTask<Player> = get()
        val players: Players = get()
        val visuals: Visuals = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        every { player.visuals } returns visuals
        players.add(0, player)
        // When
        every { visuals.flag } returns 0
        runBlocking {
            updateTask.update(visuals).await()
        }
        // Then
        verify { visuals.update = null }
        verify(exactly = 0) { visuals.encoded = any() }
    }

    @Test
    fun `Update writes addition if any addMasks changed`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val visuals: Visuals = mockk(relaxed = true)
        every { visuals.flag } returns 1
        every { visuals.flagged(any()) } returns true
        every { updateTask.updateVisuals(visuals) } just Runs
        every { updateTask.encodeUpdate(visuals) } just Runs
        every { updateTask.encodeAddition(visuals) } just Runs
        // When
        runBlocking {
            updateTask.update(visuals).await()
        }
        // Then
        verifyOrder {
            updateTask.updateVisuals(visuals)
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
            updateTask.updateVisuals(visuals)
            updateTask.encodeUpdate(visuals)
            visuals.flag = 0
        }
    }

    @Test
    fun `Update visuals that are flagged`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val visuals: Visuals = mockk(relaxed = true)
        val visual: Visual = mockk(relaxed = true)
        val mask = 0x10
        every { encoder.mask } returns mask
        every { visuals.flagged(mask) } returns true
        every { visuals.aspects[mask] } returns visual
        every { visuals.encoded.containsKey(mask) } returns false
        // When
        updateTask.updateVisuals(visuals)
        // Then
        verifyOrder {
            encoder.encode(any(), visual)
            visuals.encoded[mask] = any()
        }
    }

    @Test
    fun `Update visuals that are additions and blank`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val visuals: Visuals = mockk(relaxed = true)
        val visual: Visual = mockk(relaxed = true)
        val mask = 0x8
        every { encoder.mask } returns mask
        every { visuals.flagged(mask) } returns true
        every { visuals.aspects[mask] } returns visual
        every { visuals.encoded.containsKey(mask) } returns false
        // When
        updateTask.updateVisuals(visuals)
        // Then
        verifyOrder {
            encoder.encode(any(), visual)
            visuals.encoded[mask] = any()
        }
    }

    @Test
    fun `Don't update visuals that aren't flagged or blank`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val visuals: Visuals = mockk(relaxed = true)
        val visual: Visual = mockk(relaxed = true)
        val encoded = visuals.encoded
        val mask = 0x8
        every { encoder.mask } returns mask
        every { visuals.flagged(mask) } returns false
        every { visuals.aspects[mask] } returns visual
        every { visuals.encoded.containsKey(mask) } returns true
        // When
        updateTask.updateVisuals(visuals)
        // Then
        verify(exactly = 0) {
            encoder.encode(any(), visual)
            encoded[mask] = any()
        }
    }

    @Test
    fun `Encode flagged update`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val visuals: Visuals = mockk(relaxed = true)
        val array = byteArrayOf(1, 2, 3, 4)
        var data: ByteArray? = null
        val mask = 0x8
        every { encoder.mask } returns mask
        every { visuals.flag } returns mask
        every { visuals.encoded[mask] } returns array
        every { visuals.flagged(mask) } returns true
        every { visuals.update = any() } answers {
            data = arg(0)
        }
        // When
        updateTask.encodeUpdate(visuals)
        // Then
        verifyOrder {
            updateTask.writeFlag(any(), 0x8, 0x800)
            visuals.update = any()
        }
        assertNotNull(data)
        assert(data!!.contentEquals(byteArrayOf(8, 1, 2, 3, 4)))
    }

    @Test
    fun `Encode ignores not flagged update`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val visuals: Visuals = mockk(relaxed = true)
        val array = byteArrayOf(1, 2, 3, 4)
        var data: ByteArray? = null
        val mask = 0x8
        every { encoder.mask } returns mask
        every { visuals.flag } returns mask
        every { visuals.encoded[mask] } returns array
        every { visuals.flagged(mask) } returns false
        every { visuals.update = any() } answers {
            data = arg(0)
        }
        // When
        updateTask.encodeUpdate(visuals)
        // Then
        verifyOrder {
            updateTask.writeFlag(any(), 0x8, 0x800)
            visuals.update = any()
        }
        assertNotNull(data)
        assert(data!!.contentEquals(byteArrayOf(8)))
    }

    @Test
    fun `Encode addition`() {
        // Given
        val updateTask: VisualsEncodeTask<Player> = get()
        val visuals: Visuals = mockk(relaxed = true)
        val mask = addMasks.first()
        val array = byteArrayOf(1, 2, 3, 4)
        var data: ByteArray? = null
        every { visuals.encoded[mask] } returns array
        every { visuals.addition = any() } answers {
            data = arg(0)
        }
        // When
        updateTask.encodeAddition(visuals)
        // Then
        verifyOrder {
            updateTask.writeFlag(any(), addMasks.sum(), 0x800)
            visuals.addition = any()
        }
        assertNotNull(data)
        assert(data!!.contentEquals(byteArrayOf(8, 1, 2, 3, 4)))
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