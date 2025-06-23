package world.gregs.voidps.engine.entity.obj

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GameObjectTest {

    @Test
    fun `Get values from hash`() {
        val hash = GameObject.pack(43200, 12000, 9600, 3, 22, 3)
        assertEquals(43200, GameObject.id(hash))
        assertEquals(12000, GameObject.x(hash))
        assertEquals(9600, GameObject.y(hash))
        assertEquals(3, GameObject.level(hash))
        assertEquals(22, GameObject.shape(hash))
        assertEquals(3, GameObject.rotation(hash))
    }
}
