package rs.dusk.engine.map

import rs.dusk.engine.map.area.Coordinate3D
import kotlin.math.abs
import kotlin.math.sqrt

/**
 *  A static kd-tree which stores several [planes] of 2d-kd-tree's for nearest neighbour searches
 */
class KDTree<T : Coordinate3D>(private val planes: Int, points: List<T>) {

    private data class Node<T : Coordinate3D>(val point: T, val left: Node<T>? = null, val right: Node<T>? = null)

    private val roots: Array<Node<T>?> = arrayOfNulls(planes)

    var empty = true
        private set

    init {
        for ((plane, list) in points.groupBy { it.plane }) {
            if (invalidPlane(plane)) {
                continue
            }
            val node = build(list, 0)
            roots[plane] = node
            if (node != null) {
                empty = false
            }
        }
    }

    private fun invalidPlane(plane: Int) = plane >= planes

    private fun build(list: List<T>, depth: Int): Node<T>? {
        if (list.isEmpty()) {
            return null
        }
        val axis = depth.rem(2)
        val points = list.sortedBy { it[axis] }
        val point = points[points.size / 2]
        val left = build(points.subList(0, points.size / 2), depth + 1)
        val right = build(points.subList(points.size / 2 + 1, points.size), depth + 1)
        return Node(point, left, right)
    }

    fun nearestNeighbour(tile: T): T? {
        if (invalidPlane(tile.plane)) {
            return null
        }
        val root = roots[tile.plane] ?: return null
        return nearest(root, tile, 0)
    }

    private fun nearest(root: Node<T>?, point: T, depth: Int): T? {
        if (root == null) {
            return null
        }

        val axis = depth.rem(2)
        val closer = closer(point, root.point, axis)

        val best = closest(point, closer, root.right, root.left, depth, root.point)
        if (best == null || exceedsRootBounds(point, best, root.point, axis)) {
            return closest(point, closer, root.left, root.right, depth, best)
        }
        return best
    }

    private fun closer(first: T, second: T, axis: Int): Boolean {
        return first[axis] < second[axis]
    }

    private fun closest(point: T, closer: Boolean, right: Node<T>?, left: Node<T>?, depth: Int, other: T?): T? {
        val next = if (closer) right else left
        val nearest = nearest(next, point, depth + 1)
        return closestFrom(point, nearest, other)
    }

    private fun exceedsRootBounds(point: T, best: T, root: T, axis: Int) =
        distance(point, best) > abs(point[axis] - root[axis])

    private fun closestFrom(origin: T, p1: T?, p2: T?): T? {
        if (p1 == null) {
            return p2
        }
        if (p2 == null) {
            return p1
        }

        val d1 = distance(origin, p1)
        val d2 = distance(origin, p2)

        return if (d1 < d2) p1 else p2
    }

    private fun distance(first: T, second: T): Double {
        val dx = second.x - first.x
        val dy = second.y - first.y
        return sqrt((dx * dx + dy * dy).toDouble())
    }

    private operator fun T.get(axis: Int): Int {
        return if (axis == 0) x else y
    }
}