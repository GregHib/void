package rs.dusk.engine.map

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class KDTreeTest {

    @Test
    fun `Empty tree`() {
        val tree = KDTree<Tile>(0, listOf())
        assertTrue(tree.empty)
    }

    @Test
    fun `Kd tree with one point`() {
        val tree = KDTree(1, listOf(Tile(1)))
        assertFalse(tree.empty)
    }

    @Test
    fun `Kd tree nearest neighbour`() {
        val tree = KDTree(1, listOf(Tile(1, 0)))
        assertEquals(Tile(1, 0), tree.nearestNeighbour(Tile(10, 0)))
    }

    @Test
    fun `Nearest neighbour of two points`() {
        val tree = KDTree(1, listOf(Tile(1, 0), Tile(4, 0)))
        assertEquals(Tile(4, 0), tree.nearestNeighbour(Tile(3, 0)))
    }

    @Test
    fun `Nearest neighbour of two equidistant points`() {
        val tree = KDTree(1, listOf(Tile(1, 0), Tile(3, 0)))
        assertEquals(Tile(3, 0), tree.nearestNeighbour(Tile(2, 0)))
    }

    @Test
    fun `Nearest neighbour of two two-dimensional points`() {
        val tree = KDTree(1, listOf(Tile(0, 2), Tile(3, 1)))
        assertEquals(Tile(3, 1), tree.nearestNeighbour(Tile(2, 2)))
    }

    @Test
    fun `Point on other planes aren't considered`() {
        val tree = KDTree(3, listOf(Tile(0, 2), Tile(1, 1, 1)))
        assertEquals(Tile(0, 2), tree.nearestNeighbour(Tile(0, 0)))
    }

    @Test
    fun `Invalid plane returns null`() {
        val tree = KDTree(2, listOf(Tile(0, 0)))
        assertNull(tree.nearestNeighbour(Tile(0, 0, 2)))
    }

    @Test
    fun `Nearest neighbour of multiple points`() {
        val tree = KDTree(1, listOf(
            Tile(2, 3),
            Tile(5, 4),
            Tile(9, 6),
            Tile(4, 7),
            Tile(8, 1),
            Tile(7, 2),
            Tile(6, 3)
        ))
        assertEquals(Tile(6, 3), tree.nearestNeighbour(Tile(7, 5)))
    }
}