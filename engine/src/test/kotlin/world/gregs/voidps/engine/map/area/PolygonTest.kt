package world.gregs.voidps.engine.map.area

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PolygonTest {

    @Test
    fun `Area of convex polygon`() {
        val area = Polygon(intArrayOf(4, 4, 8, 8, -4, -4), intArrayOf(6, -4, -4, -8, -8, 6))
        assertEquals(128.0, area.area)
    }

    @Test
    fun `Area of concave polygon`() {
        val area = Polygon(intArrayOf(11, 8, 10, 4), intArrayOf(10, 7, 4, 6))
        assertEquals(11.5, area.area)
    }

}