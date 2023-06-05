package world.gregs.voidps.engine.map.chunk

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.file.MapExtract
import world.gregs.voidps.engine.map.region.Region

internal class DynamicChunksTest {

    private lateinit var chunks: DynamicChunks
    private lateinit var objects: GameObjects
    private lateinit var collisions: Collisions
    private lateinit var extract: MapExtract

    @BeforeEach
    fun setup() {
        objects = mockk(relaxed = true)
        collisions = mockk(relaxed = true)
        extract = mockk(relaxed = true)
        chunks = DynamicChunks(objects, collisions, extract)
    }

    @Test
    fun `Copy one chunk to another`() {
        val to = Chunk(8, 8)
        chunks.copy(Chunk(4, 4), to)

        assertTrue(chunks.isDynamic(to.region))
        assertEquals(65568, chunks.getDynamicChunk(to))
    }

    @Test
    fun `Copy one chunk to itself with rotation`() {
        val chunk = Chunk(8, 8)
        chunks.copy(chunk, chunk, rotation = 2)

        assertTrue(chunks.isDynamic(chunk.region))
        assertEquals(131140, chunks.getDynamicChunk(chunk))
    }

    @Test
    fun `Copy one region to another`() {
        val from = Region(8, 8)
        val to = Region(42, 42)
        chunks.copy(from, to)

        assertFalse(chunks.isDynamic(from))
        assertTrue(chunks.isDynamic(to))
        assertEquals(1049088, chunks.getDynamicChunk(to.tile.chunk))
        assertEquals(1163832, chunks.getDynamicChunk(to.tile.chunk.add(7, 7)))
    }

    @Test
    fun `Reset a chunk`() {
        val chunk = Chunk(4, 4)
        chunks.copy(chunk, chunk, 2)
        assertTrue(chunks.isDynamic(chunk.region))
        chunks.clear(chunk)

        assertFalse(chunks.isDynamic(chunk.region))
        assertNull(chunks.getDynamicChunk(chunk))
    }

    @Test
    fun `Reset a region`() {
        val region = Region(8, 8)
        chunks.copy(region, region)
        assertTrue(chunks.isDynamic(region))
        chunks.clear(region)

        assertFalse(chunks.isDynamic(region))
        assertNull(chunks.getDynamicChunk(region.tile.chunk))
    }
}