package world.gregs.voidps.engine.entity.character

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CharacterListTest {

    private lateinit var list: CharacterList<Character>
    private lateinit var characterMap: CharacterMap

    @BeforeEach
    fun setup() {
        characterMap = mockk(relaxed = true)
        list = object : CharacterList<Character>(10) {
            override val indexArray: Array<Character?> = arrayOfNulls(10)
            override fun get(tile: Tile): List<Character> {
                return emptyList()
            }

            override fun get(zone: Zone): List<Character> {
                return emptyList()
            }

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
    }

    @Test
    fun `Clear all characters in list`() {
        val character: Character = mockk(relaxed = true)
        every { character.index } returns 1
        every { character.tile } returns Tile(1)
        assertTrue(list.add(character))
        list.clear()

        assertEquals(0, list.size)
    }

}