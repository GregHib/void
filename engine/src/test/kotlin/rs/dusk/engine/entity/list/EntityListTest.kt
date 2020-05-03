package rs.dusk.engine.entity.list

import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.entity.model.Entity
import rs.dusk.engine.model.Tile

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 22, 2020
 */
internal class EntityListTest {

    lateinit var list: EntityList<Entity>
    lateinit var entity: Entity
    val tile = Tile(10, 20, 1)
    val hash = tile.id

    @BeforeEach
    fun setup() {
        list = spyk(object : EntityList<Entity> {
            override fun get(hash: Int): Set<Entity>? = null

            override fun remove(hash: Int, entity: Entity): Boolean = true

            override fun forEach(action: (Entity) -> Unit) {
            }

            override fun add(hash: Int, entity: Entity): Boolean = true

        })
        entity = mockk()

        every { entity.tile } returns tile
    }

    @Test
    fun `Get tile`() {
        // When
        list[tile]
        // Then
        verify { list[hash] }
    }

    @Test
    fun `Get coordinates`() {
        // When
        list[10, 20, 1]
        // Then
        verifyOrder {
            list[any<Tile>()]
            list[hash]
        }
    }

    @Test
    fun `Add tile`() {
        // When
        list.add(tile, entity)
        // Then
        verify { list.add(hash, entity) }
    }

    @Test
    fun `Add coordinates`() {
        // When
        list.add(10, 20, 1, entity)
        // Then
        verifyOrder {
            list.add(any<Tile>(), entity)
            list.add(hash, entity)
        }
    }

    @Test
    fun `Set adds`() {
        // When
        list[hash] = entity
        // Then
        verify { list.add(hash, entity) }
    }

    @Test
    fun `Set tile`() {
        // When
        list[tile] = entity
        // Then
        verify { list.add(hash, entity) }
    }

    @Test
    fun `Set coordinates`() {
        // When
        list[10, 20, 1] = entity
        // Then
        verifyOrder {
            list.add(hash, entity)
        }
    }

    @Test
    fun `Remove tile`() {
        // When
        list.remove(tile, entity)
        // Then
        verify { list.remove(hash, entity) }
    }

    @Test
    fun `Remove coordinates`() {
        // When
        list.remove(10, 20, 1, entity)
        // Then
        verifyOrder {
            list.remove(any<Tile>(), entity)
            list.remove(hash, entity)
        }
    }


}