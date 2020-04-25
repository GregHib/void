package rs.dusk.engine.view

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import rs.dusk.engine.EngineTasks
import rs.dusk.engine.engineModule
import rs.dusk.engine.entity.list.entityListModule
import rs.dusk.engine.entity.list.npc.NPCs
import rs.dusk.engine.entity.list.player.Players
import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.model.Tile
import rs.dusk.engine.script.KoinMock
import rs.dusk.utility.get
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
internal class ViewportTaskTest : KoinMock() {

    override val modules = listOf(engineModule, entityListModule, viewportModule)

    @Test
    fun `Viewport task adds itself to engine tasks`() {
        // Given
        val tasks: EngineTasks = get()
        val viewportTask: ViewportTask = get()
        // Then
        assert(tasks.contains(viewportTask))
    }

    @Test
    fun `Add to empty viewport`() {
        // Given
        val task: ViewportTask = get()
        val viewport = setupViewport()
        val entity = addEntity(1, 5, 0)
        addToList(entity)
        // When
        task.run()
        // Then
        assert(viewport.npcs.add.contains(entity))
    }

    @Test
    fun `Add to busy viewport`() {
        // Given
        val task: ViewportTask = get()
        val viewport = setupViewport()
        addToView(viewport, addToList(addEntity(1, 1, 0)))
        addToView(viewport, addToList(addEntity(2, 1, 0)))
        addToView(viewport, addToList(addEntity(1, 2, 0)))
        val entity = addEntity(0, 0, 0)
        addToList(entity)
        // When
        task.run()
        // Then
        assert(viewport.npcs.add.contains(entity))
    }

    @Test
    fun `Add inside view of full viewport`() {
        // Given
        val task: ViewportTask = get()
        val viewport = setupViewport()
        repeat(256) {
            addToView(viewport, addToList(addEntity(1, 1, 0)))
        }
        val entity = addEntity(0, 0, 0)
        addToList(entity)
        // When
        task.run()
        // Then
        assert(viewport.npcs.add.contains(entity))
    }

    @Test
    fun `Add outside view of full viewport`() {
        // Given
        val task: ViewportTask = get()
        val viewport = setupViewport()
        repeat(256) {
            addToView(viewport, addToList(addEntity(1, 1, 0)))
        }
        val entity = addEntity(2, 2, 0)
        addToList(entity)
        // When
        task.run()
        // Then
        assert(!viewport.npcs.add.contains(entity))
    }

    @Test
    fun `Add to same tile as another entity`() {
        // Given
        val task: ViewportTask = get()
        val viewport = setupViewport()
        addToView(viewport, addToList(addEntity(5, 5, 0)))
        val entity = addEntity(5, 5, 0)
        addToList(entity)
        // When
        task.run()
        // Then
        assert(viewport.npcs.add.contains(entity))
    }

    @Test
    fun `Add to inner edge of max view distance`() {
        // Given
        val task: ViewportTask = get()
        val viewport = setupViewport()
        val entity = addEntity(15, 15, 0)
        addToList(entity)
        // When
        task.run()
        // Then
        assert(viewport.npcs.add.contains(entity))
    }

    @Test
    fun `Add to outer edge of max view distance`() {
        // Given
        val task: ViewportTask = get()
        val viewport = setupViewport()
        val entity = addEntity(16, 16, 0)
        addToList(entity)
        // When
        task.run()
        // Then
        assert(!viewport.npcs.add.contains(entity))
    }

    @Test
    fun `Remove from viewport`() {
        // Given
        val task: ViewportTask = get()
        val viewport = setupViewport()
        val entity = addEntity(1, 1, 0)
        addToView(viewport, entity)
        // When
        task.run()
        // Then
        assert(viewport.npcs.remove.contains(entity))
    }

    @Test
    fun `Remove from busy viewport`() {
        // Given
        val task: ViewportTask = get()
        val viewport = setupViewport()
        addToView(viewport, addToList(addEntity(1, 1, 0)))
        addToView(viewport, addToList(addEntity(2, 1, 0)))
        addToView(viewport, addToList(addEntity(1, 2, 0)))
        val entity = addEntity(1, 1, 0)
        addToView(viewport, entity)
        // When
        task.run()
        // Then
        assert(viewport.npcs.remove.contains(entity))
    }

    @Test
    fun `Remove from same tile as another entity`() {
        // Given
        val task: ViewportTask = get()
        val viewport = setupViewport()
        addToView(viewport, addToList(addEntity(1, 1, 0)))
        val entity = addEntity(1, 1, 0)
        addToView(viewport, entity)
        // When
        task.run()
        // Then
        assert(viewport.npcs.remove.contains(entity))
    }

    @Test
    fun `Remove from inner edge of max view distance`() {
        // Given
        val task: ViewportTask = get()
        val viewport = setupViewport()
        val entity = addEntity(15, 15, 0)
        addToView(viewport, entity)
        // When
        task.run()
        // Then
        assert(viewport.npcs.remove.contains(entity))
    }

    @Test
    fun `Remove from outer edge of max view distance`() {
        // Given
        val task: ViewportTask = get()
        val viewport = setupViewport()
        val entity = addEntity(16, 16, 0)
        addToView(viewport, entity)
        // When
        task.run()
        // Then
        assert(viewport.npcs.remove.contains(entity))
    }

    fun setupViewport(): Viewport {
        val viewer = mockk<Player>(relaxed = true)
        val viewport = Viewport()
        every { viewer.viewport } returns viewport
        val players: Players = get()
        players[0x40000000] = viewer//0, 0, 4
        return viewport
    }

    val counter = AtomicInteger(0)

    fun addEntity(x: Int, y: Int, plane: Int): NPC {
//        val npc: NPC = mockk(relaxed = true)// FIXME #46 can't mock tile
        val tile = Tile(x, y, plane)
        val npc = NPC(counter.getAndIncrement(), tile)
        return npc
    }

    fun addToView(viewport: Viewport, npc: NPC): NPC {
        viewport.npcs.current.add(npc)
        return npc
    }

    fun addToList(npc: NPC): NPC {
        val npcs: NPCs = get()
        npcs[npc.tile] = npc
        npcs[npc.tile.chunk] = npc
        return npc
    }
}