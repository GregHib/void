package rs.dusk.engine.map.area

import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.collision.Collisions

internal class LineOfSightTest {

    lateinit var los: LineOfSight
    lateinit var data: MutableMap<Int, Int>
    lateinit var collisions: Collisions

    @BeforeEach
    fun setup() {
        data = spyk(mutableMapOf())
        collisions = spyk(Collisions(data))
        los = LineOfSight(collisions)
    }

    @Test
    fun `Has line of sight if under`() {
        val tile = Tile(0, 0)
        val other = Tile(0, 0)
        // Then
        assertTrue(los.withinSight(tile, other))
    }

    @Test
    fun `No line of sight on other plane`() {
        val tile = Tile(0, 0)
        val other = Tile(0, 0, 1)
        // Then
        assertFalse(los.withinSight(tile, other))
    }

    @Test
    fun `Has line of sight over fence`() {
        data[Tile.getId(0, 0, 0)] = 34603016
        data[Tile.getId(1, 0, 0)] = 537919616
        val tile = Tile(0, 0)
        val other = Tile(1, 0)
        // Then
        assertTrue(los.withinSight(tile, other))
    }

    @Test
    fun `No line of sight over wall`() {
        data[Tile.getId(0, 0)] = 34607112
        data[Tile.getId(1, 0)] = 537985152
        val tile = Tile(0, 0)
        val other = Tile(1, 0)
        // Then
        assertFalse(los.withinSight(tile, other))
    }

    @Test
    fun `Has line of sight over distant bush`() {
        for(x in 2..4) {
            for(y in 2..4) {
                data[Tile.getId(x, y)] = 1048576
            }
        }
        data[Tile.getId(3, 3)] = 1074790656
        val tile = Tile(0, 0)
        val other = Tile(5, 5)
        // Then
        assertTrue(los.withinSight(tile, other))
    }

    @Test
    fun `Has diagonal line of sight over fence`() {
        for(x in 0..4) {
            data[Tile.getId(x, 1)] = 135266336
            data[Tile.getId(x, 0)] = 9437186
        }
        val tile = Tile(0, 0)
        val other = Tile(1, 2)
        // Then
        assertTrue(los.withinSight(tile, other))
    }

    @Test
    fun `No diagonal line of sight over wall`() {
        for(x in 0..4) {
            data[Tile.getId(x, 1)] = 135282720
            data[Tile.getId(x, 0)] = 9438210
        }
        val tile = Tile(0, 0)
        val other = Tile(1, 2)
        // Then
        assertFalse(los.withinSight(tile, other))
    }

    /**
     *  |A|-|-|-|-|
     *  |-|\|-|T|T|
     *  |-|-|x|T|T|
     *  |-|-|-|-|B|
     */
    @Test
    fun `No horizontal line of sight behind tree`() {
        for(x in 2..5) {
            for(y in 0..3) {
                data[Tile.getId(x, y)] = 1048576
            }
        }
        for(x in 3..4) {
            for(y in 1..2) {
                data[Tile.getId(x, y)] = 1074921728
            }
        }
        val tile = Tile(0, 3)
        val other = Tile(4, 0)
        // Then
        assertFalse(los.withinSight(tile, other))
    }

    /**
     *  |-|-|B|-|
     *  |-|x|T|T|
     *  |-|-|T|T|
     *  |-|/|-|-|
     *  |A|-|-|-|
     */
    @Test
    fun `No vertical line of sight behind tree`() {
        for(x in 1..4) {
            for(y in 1..4) {
                data[Tile.getId(x, y)] = 1048576
            }
        }
        for(x in 2..3) {
            for(y in 2..3) {
                data[Tile.getId(x, y)] = 1074921728
            }
        }
        val tile = Tile(0, 0)
        val other = Tile(3, 4)
        // Then
        assertFalse(los.withinSight(tile, other))
    }

}