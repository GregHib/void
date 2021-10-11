package world.gregs.voidps.engine.client.update.task

import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.engine.client.update.task.player.PlayerVisualsTask
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.update.Visual
import world.gregs.voidps.engine.entity.character.update.VisualEncoder
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.entity.list.PooledMapList
import world.gregs.voidps.engine.entity.list.entityListModule
import world.gregs.voidps.engine.event.eventModule
import world.gregs.voidps.engine.script.KoinMock
import world.gregs.voidps.engine.utility.get

internal class PlayerVisualsTaskTest : KoinMock() {

    private val encoder: VisualEncoder<Visual> = mockk(relaxed = true)

    init {
        every { encoder.mask } returns 0x8
    }

    private val addMasks = intArrayOf(encoder.mask)
    private val players: PooledMapList<Player> = mockk(relaxed = true)
    private val encoderModule = module {
        single { spyk(
            PlayerVisualsTask(
                players,
                arrayOf(encoder),
                addMasks
            )
        ) }
    }
    override val modules = listOf(eventModule, entityListModule, encoderModule)

    @Test
    fun `Run runs all in parallel`() {
        // Given
        val updateTask: PlayerVisualsTask = get()
        val player: Player = mockk(relaxed = true)
        every { players.forEach(any()) } answers {
            arg<(Character) -> Unit>(0).invoke(player)
        }
        val visuals: Visuals = mockk(relaxed = true)
        every { player.visuals } returns visuals
        // When
        updateTask.run()
        // Then
        coVerify {
            updateTask.runAsync(player)
        }
    }

    @Test
    fun `Update skips if un-flagged`() {
        // Given
        val task: PlayerVisualsTask = get()
        val players: Players = get()
        val visuals: Visuals = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        every { player.visuals } returns visuals
        players.add(0, player)
        // When
        every { visuals.flag } returns 0
        task.runAsync(player)
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
        val updateTask: PlayerVisualsTask = get()
        val visuals: Visuals = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        every { player.visuals } returns visuals
        every { visuals.flag } returns 1
        every { visuals.flagged(any()) } returns true
        every { updateTask.encodeUpdate(visuals) } just Runs
        every { updateTask.encodeAddition(visuals) } just Runs
        // When
        updateTask.runAsync(player)
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
        val updateTask: PlayerVisualsTask = get()
        val visuals: Visuals = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        every { player.visuals } returns visuals
        every { visuals.flag } returns 1
        every { visuals.flagged(any()) } returns false
        // When
        updateTask.runAsync(player)
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
        val updateTask: PlayerVisualsTask = get()
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
        val updateTask: PlayerVisualsTask = get()
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
        val updateTask: PlayerVisualsTask = get()
        val visuals: Visuals = mockk(relaxed = true)
        val map: MutableMap<Int, Visual> = mockk(relaxed = true)
        every { visuals.aspects } returns map
        every { map[any()] } returns mockk(relaxed = true)
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
        val updateTask: PlayerVisualsTask = get()
        val writer = BufferWriter()
        // When
        updateTask.writeFlag(writer, 0x10)
        // Then
        val reader = BufferReader(writer.array())
        assertEquals(0x10, reader.readByte())
    }

    @Test
    fun `Write medium flag`() {
        // Given
        val updateTask: PlayerVisualsTask = get()
        val writer = BufferWriter()
        // When
        updateTask.writeFlag(writer, 0x100)
        // Then
        val reader = BufferReader(writer.array())
        assertEquals(0x40, reader.readUnsignedByte())
        assertEquals(0x1, reader.readUnsignedByte())
    }

    @Test
    fun `Write large flag`() {
        // Given
        val updateTask: PlayerVisualsTask = get()
        val writer = BufferWriter()
        // When
        updateTask.writeFlag(writer, 0x10000)
        // Then
        val reader = BufferReader(writer.array())
        assertEquals(0x40, reader.readUnsignedByte())
        assertEquals(0x40, reader.readUnsignedByte())
        assertEquals(0x1, reader.readUnsignedByte())
    }
}