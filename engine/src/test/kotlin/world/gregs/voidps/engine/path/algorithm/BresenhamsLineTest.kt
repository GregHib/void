package world.gregs.voidps.engine.path.algorithm

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.strategy.IgnoredCollision
import world.gregs.voidps.engine.map.collision.strategy.LandCollision
import world.gregs.voidps.engine.map.collision.strategy.SkyCollision
import world.gregs.voidps.engine.map.region.RegionPlane

internal class BresenhamsLineTest {

    lateinit var regions: IntArray
    lateinit var los: BresenhamsLine
    lateinit var data: Array<IntArray?>

    @BeforeEach
    fun setup() {
        regions = IntArray(1) { -1 }
        regions[0] = 0
        data = arrayOfNulls(1)
        val collisions = Collisions(regions, data)
        val land = LandCollision(collisions)
        los = BresenhamsLine(SkyCollision(collisions), land, IgnoredCollision(collisions, land))
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
        set(0, 0, 34603016)
        set(1, 0, 537919616)
        val tile = Tile(0, 0)
        val other = Tile(1, 0)
        // Then
        assertTrue(los.withinSight(tile, other))
    }

    @Test
    fun `No line of sight over wall`() {
        set(0, 0, 34607112)
        set(1, 0, 537985152)
        val tile = Tile(0, 0)
        val other = Tile(1, 0)
        // Then
        assertFalse(los.withinSight(tile, other))
    }

    @Test
    fun `No line of sight over distant ignored bush`() {
        for (x in 2..4) {
            for (y in 2..4) {
                set(x, y, 1048576)
            }
        }
        set(3, 3, 1074790656)
        val tile = Tile(0, 0)
        val other = Tile(5, 5)

        // Then
        assertFalse(los.withinSight(tile, other))
    }

    @Test
    fun `Has diagonal line of sight over fence`() {
        for (x in 0..4) {
            set(x, 1, 135266336)
            set(x, 0, 9437186)
        }
        val tile = Tile(0, 0)
        val other = Tile(1, 2)
        // Then
        assertTrue(los.withinSight(tile, other))
    }

    @Test
    fun `No diagonal line of sight over wall`() {
        for (x in 0..4) {
            set(x, 1, 135282720)
            set(x, 0, 9438210)
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
        for (x in 2..5) {
            for (y in 0..3) {
                set(x, y, 1048576)
            }
        }
        for (x in 3..4) {
            for (y in 1..2) {
                set(x, y, 1074921728)
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
        for (x in 1..4) {
            for (y in 1..4) {
                set(x, y, 1048576)
            }
        }
        for (x in 2..3) {
            for (y in 2..3) {
                set(x, y, 1074921728)
            }
        }
        val tile = Tile(0, 0)
        val other = Tile(3, 4)
        // Then
        assertFalse(los.withinSight(tile, other))
    }

    private fun set(x: Int, y: Int, value: Int) {
        val index = x * 64 + y
        val id = RegionPlane.getId(0, 0, 0)
        if (data[regions[id]] == null) {
            data[regions[id]] = IntArray(4096)
        }
        data[regions[id]]!![index] = value
    }
}