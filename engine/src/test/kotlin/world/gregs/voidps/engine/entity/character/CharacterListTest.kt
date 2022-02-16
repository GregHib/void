package world.gregs.voidps.engine.entity.character

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.TileMap
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CharacterListTest {

    private lateinit var list: CharacterList<Character>
    private lateinit var tileMap: TileMap<Character>

    @BeforeEach
    fun setup() {
        tileMap = mockk(relaxed = true)
        list = object : CharacterList<Character>(10, tileMap) {
            override val indexArray: Array<Character?> = arrayOfNulls(10)
        }
    }

    @Test
    fun `Add character to list`() {
        val character: Character = mockk(relaxed = true)
        every { character.index } returns 1
        every { character.tile } returns Tile(1)
        assertTrue(list.add(character))

        assertEquals(character, list.indexed(1))
        assertEquals(1, list.size)
        assertEquals(1, list.count(character.tile.chunk))
    }

    @Test
    fun `Remove character from list`() {
        val character: Character = mockk(relaxed = true)
        every { character.index } returns 1
        every { character.tile } returns Tile(1)
        assertTrue(list.add(character))

        assertTrue(list.remove(character))

        assertNotNull(list.indexed(1))
        assertEquals(0, list.size)
    }

    @Test
    fun `Remove index from list`() {
        val character: Character = mockk(relaxed = true)
        every { character.index } returns 1
        every { character.tile } returns Tile(1)
        assertTrue(list.add(character))

        list.removeIndex(character)

        assertNull(list.indexed(1))
        assertEquals(1, list.size)
        assertEquals(0, list.count(character.tile.chunk))
    }

    @Test
    fun `Update character position`() {
        val character: Character = mockk(relaxed = true)
        every { character.index } returns 1
        every { character.tile } returns Tile(1)
        assertTrue(list.add(character))
        list.update(Tile(1), Tile(2), character)

        verify {
            tileMap.remove(Tile(1), character)
            tileMap.add(Tile(2), character)
        }
    }

    @Test
    fun `Clear all characters in list`() {
        val character: Character = mockk(relaxed = true)
        every { character.index } returns 1
        every { character.tile } returns Tile(1)
        assertTrue(list.add(character))
        list.clear()

        assertEquals(0, list.size)
        assertEquals(0, list.count(character.tile.chunk))
    }

}