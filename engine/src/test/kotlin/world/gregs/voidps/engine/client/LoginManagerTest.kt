package world.gregs.voidps.engine.client

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.entity.character.IndexAllocator

class LoginManagerTest {

    private lateinit var manager: LoginManager
    private lateinit var indices: IndexAllocator

    @BeforeEach
    fun setup() {
        indices = IndexAllocator(5)
        manager = LoginManager(indices)
    }

    @Test
    fun `Same names can't be added twice`() {
        val name = "player"
        assertEquals(0, manager.count(name))
        assertEquals(1, manager.add(name))
        assertEquals(1, manager.count(name))
        assertNull(manager.add(name))
        assertEquals(1, manager.count(name))
    }

    @Test
    fun `Different names different count`() {
        val name1 = "player1"
        val name2 = "player2"
        assertEquals(0, manager.count(name1))
        assertEquals(1, manager.add(name1))
        assertEquals(1, manager.count(name1))
        assertEquals(0, manager.count(name2))
        assertEquals(2, manager.add(name2))
        assertEquals(1, manager.count(name2))
    }

    @Test
    fun `Removed named clear count`() {
        val name = "player"
        assertEquals(1, manager.add(name))
        assertEquals(1, manager.count(name))
        manager.remove(name)
        assertEquals(0, manager.count(name))
    }

    @Test
    fun `Removing name twice doesn't cause negative count`() {
        val name = "player"
        assertEquals(1, manager.add(name))
        assertEquals(1, manager.count(name))
        manager.remove(name)
        assertEquals(0, manager.count(name))
        manager.remove(name)
        assertEquals(0, manager.count(name))
    }

    @Test
    fun `Clear names removes count`() {
        val name = "player"
        assertEquals(1, manager.add(name))
        assertEquals(1, manager.count(name))
        manager.clear()
        assertEquals(0, manager.count(name))
        assertEquals(1, manager.add(name))
    }
}