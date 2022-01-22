package world.gregs.voidps.engine.entity.list

import io.mockk.every
import io.mockk.mockk
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.map.Tile
import java.util.*

internal class PooledMapListTest {

    lateinit var list: PooledMapList<Character>
    lateinit var entity: Character
    var hash: Int = -1

    @BeforeEach
    fun setup() {
        list = object : PooledMapList<Character> {
            override val data: Int2ObjectOpenHashMap<ObjectLinkedOpenHashSet<Character>> = Int2ObjectOpenHashMap()
            override val pool: LinkedList<ObjectLinkedOpenHashSet<Character>> = LinkedList()
            override val indexed: Array<Character?> = arrayOfNulls(10)
        }
        entity = mockk()
        val tile = Tile(10, 20, 1)
        hash = tile.id
        every { entity.tile } returns tile
    }

    @Test
    fun `Add uses pooled tile if none exist`() {
        // Given
        val set = ObjectLinkedOpenHashSet<Character>()
        list.pool.push(set)
        // When
        list.add(hash, entity)
        // Then
        val result = list.data[hash]
        assertNotNull(result)
        assertEquals(set, result)
        assert(result.contains(entity))
    }

    @Test
    fun `Add creates tile set if pool is empty`() {
        // When
        list.add(hash, entity)
        // Then
        val result = list.data[hash]
        assertNotNull(result)
        assert(result.contains(entity))
    }

    @Test
    fun `Get returns empty`() {
        // When
        val result = list[hash]
        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun get() {
        // Given
        val set = ObjectLinkedOpenHashSet<Character>()
        set.add(entity)
        list.data[hash] = set
        // When
        val result = list[hash]
        // Then
        assertNotNull(result)
        assertEquals(set, result)
        assert(result.contains(entity))
    }

    @Test
    fun `Remove returns false if empty`() {
        // When
        val result = list.remove(hash, entity)
        // Then
        assertFalse(result)
    }

    @Test
    fun `Remove puts tile back into pool if last entity`() {
        // Given
        val set = ObjectLinkedOpenHashSet<Character>()
        set.add(entity)
        list.data[hash] = set
        // When
        val result = list.remove(hash, entity)
        // Then
        assertTrue(result)
        assert(!list.data.containsKey(hash))
        assert(list.pool.contains(set))
    }

    @Test
    fun `Remove doesn't move tile if not last entity`() {
        // Given
        val set = ObjectLinkedOpenHashSet<Character>()
        set.add(entity)
        set.add(mockk())
        list.data[hash] = set
        // When
        val result = list.remove(hash, entity)
        // Then
        assertTrue(result)
        assert(list.data.containsKey(hash))
    }

}