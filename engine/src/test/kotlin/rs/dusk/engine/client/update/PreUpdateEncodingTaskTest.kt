package rs.dusk.engine.client.update

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.qualifier.named
import org.koin.dsl.module
import rs.dusk.core.io.read.BufferReader
import rs.dusk.core.io.write.BufferWriter
import rs.dusk.engine.engineModule
import rs.dusk.engine.entity.list.entityListModule
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual
import rs.dusk.engine.entity.model.visual.VisualEncoder
import rs.dusk.engine.entity.model.visual.Visuals
import rs.dusk.engine.model.Chunk
import rs.dusk.engine.script.KoinMock
import rs.dusk.utility.get

/**
 * @author Greg Hibberd <greg></greg>@greghibberd.com>
 * @since April 26, 2020
 */
internal class PreUpdateEncodingTaskTest : KoinMock() {

    private val playerEncoder: VisualEncoder<Visual> = mockk(relaxed = true)
    private val npcEncoder: VisualEncoder<Visual> = mockk(relaxed = true)
    private val encoderModule = module {
        single(named("playerVisualEncoders")) {
            arrayOf(playerEncoder)
        }
        single(named("npcVisualEncoders")) {
            arrayOf(npcEncoder)
        }
    }

    override val modules = listOf(engineModule, entityListModule, clientUpdateModule, encoderModule)


    @Test
    fun `Run runs all in parallel`() {
        // Given
        val updateTask: PreUpdateEncodingTask = get()
        val players: Players = get()
        val npcs: NPCs = get()
        val player: Player = mockk(relaxed = true)
        val npc: NPC = mockk(relaxed = true)
        val playerVisuals: Visuals = mockk(relaxed = true)
        val npcVisuals: Visuals = mockk(relaxed = true)
        val visual: Visual = mockk(relaxed = true)
        every { player.visuals } returns playerVisuals
        every { npc.visuals } returns npcVisuals
        every { playerVisuals.aspects[any()] } returns visual
        every { npcVisuals.aspects[any()] } returns visual
        every { playerVisuals.flag } returns 0x10
        every { npcVisuals.flag } returns 0x10
        players[Chunk(0, 0)] = player
        npcs[Chunk(0, 0)] = npc
        // When
        updateTask.run()
        // Then
        verify {
            playerEncoder.encode(any(), visual)
            npcEncoder.encode(any(), visual)
            playerVisuals.encoded = any()
            npcVisuals.encoded = any()
        }
    }

    @Test
    fun `Update skips if un-flagged`() {
        // Given
        val players: Players = get()
        val visuals: Visuals = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        every { player.visuals } returns visuals
        players.add(0, player)
        // When
        every { visuals.flag } returns 0
        // Then
        verify(exactly = 0) { visuals.encoded = any() }
    }

    @Test
    fun `Update writes with encoder`() {
        // Given
        val updateTask: PreUpdateEncodingTask = get()
        val players: Players = get()
        val visuals: Visuals = mockk(relaxed = true)
        val player: Player = mockk(relaxed = true)
        val visual: Visual = mockk(relaxed = true)
        every { player.visuals } returns visuals
        every { visuals.aspects[any()] } returns visual
        players.add(Chunk(0, 0), player)
        every { visuals.flag } returns 0x100
        // When
        runBlocking {
//            updateTask.update(player, arrayOf(playerEncoder), 0x100).await() FIXME
        }
        // Then
        verify {
            playerEncoder.encode(any(), visual)
            visuals.encoded = any()
        }
    }

    @Test
    fun `Write small flag`() {
        // Given
        val updateTask: PreUpdateEncodingTask = get()
        val writer = BufferWriter()
        // When
        with(updateTask) {
            writer.writeFlag(0x10, 0x800)
        }
        // Then
        val reader = BufferReader(writer.buffer.array())
        assertEquals(0x10, reader.readByte())
    }

    @Test
    fun `Write medium flag`() {
        // Given
        val updateTask: PreUpdateEncodingTask = get()
        val writer = BufferWriter()
        // When
        with(updateTask) {
            writer.writeFlag(0x100, 0x800)
        }
        // Then
        val reader = BufferReader(writer.buffer.array())
        assertEquals(0x80, reader.readUnsignedByte())
        assertEquals(0x1, reader.readUnsignedByte())
    }

    @Test
    fun `Write large flag`() {
        // Given
        val updateTask: PreUpdateEncodingTask = get()
        val writer = BufferWriter()
        // When
        with(updateTask) {
            writer.writeFlag(0x10000, 0x800)
        }
        // Then
        val reader = BufferReader(writer.buffer.array())
        assertEquals(0x80, reader.readUnsignedByte())
        assertEquals(0x8, reader.readUnsignedByte())
        assertEquals(0x1, reader.readUnsignedByte())
    }
}