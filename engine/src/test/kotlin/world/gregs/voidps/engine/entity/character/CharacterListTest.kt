package world.gregs.voidps.engine.entity.character

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.map.RegionMap
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.region.RegionPlane
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class CharacterListTest {

    private lateinit var list: CharacterList<Character>
    private lateinit var regionMap: RegionMap

    @BeforeEach
    fun setup() {
        regionMap = mockk(relaxed = true)
        list = object : CharacterList<Character>(10, regionMap) {
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
    fun `Update character position`() {
        val index = 1
        val character: Character = mockk(relaxed = true)
        every { character.index } returns index
        every { character.tile } returns Tile(3)
        assertTrue(list.add(character))
        list.update(Tile(1), Tile(64), character)

        verify {
            regionMap.remove(RegionPlane(0), character)
            regionMap.add(RegionPlane(1), character)
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
    }

}