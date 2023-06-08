package world.gregs.voidps.engine.entity.obj

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GameObjectTest {

    @Test
    fun `Get values from hash`() {
        val hash = GameObject.getHash(43200, 12000, 9600, 3, 22, 3)
        assertEquals(43200, GameObject.getId(hash))
        assertEquals(12000, GameObject.getX(hash))
        assertEquals(9600, GameObject.getY(hash))
        assertEquals(3, GameObject.getPlane(hash))
        assertEquals(22, GameObject.getType(hash))
        assertEquals(3, GameObject.getRotation(hash))
    }
}