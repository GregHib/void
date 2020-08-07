package rs.dusk.tools.graph

import kotlinx.coroutines.runBlocking
import rs.dusk.cache.Cache
import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.entity.obj.Objects
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.area.Area3D
import rs.dusk.engine.map.area.area
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.map.region.Region
import rs.dusk.engine.map.region.RegionReader
import rs.dusk.engine.map.region.obj.Xteas
import rs.dusk.engine.path.TraversalStrategy
import rs.dusk.engine.path.TraversalType
import rs.dusk.engine.path.traverse.SmallTraversal
import rs.dusk.utility.func.isDoor
import java.io.DataOutputStream
import java.io.File
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.system.measureNanoTime

class MapGraph(
    private val reader: RegionReader,
    private val collisions: Collisions,
    private val objects: Objects,
    private val xteas: Xteas,
    private val cache: Cache
) {
    fun load(regionId: Int) {
//        216, 320 - 487, 504
        val all = mutableSetOf<Tile>()
        val objs = mutableSetOf<GameObject>()
        val links = mutableSetOf<Triple<Tile, Tile, Int>>()
        val strategy = SmallTraversal(TraversalType.Land, false, collisions)

        val reg = Region(27, 40).toPlane(0)
        runBlocking {
            for (region in reg.area(width = 33, height = 23)) {
                // TODO better way of determining empty maps
                val xtea = xteas[region.id]
                cache.getFile(5, "l${region.x}_${region.y}", xtea) ?: continue

                reader.loadAsync(region.region).await()
                for (chunk in region.chunk.area(width = 8, height = 8)) {
                    val time = measureNanoTime {
                        val loaded = objects[chunk]
                        objs.addAll(loaded)
                        all.addAll(getCenterPoints(strategy, chunk.tile.area(width = 16, height = 16)))
                    }
                    println("Objects ${objs.size} Points ${all.size} Took ${time}ns")
                }
            }
        }

        val portals = getPortals(objs)
        println("${portals.size} portals found")
        for(portal in portals) {
            all.add(portal.first)
            all.add(portal.second)
            links.add(Triple(portal.first, portal.second, 1))
//            links.add(Triple(portal.second, portal.first, 1))
        }
        links.addAll(getStaticLinks(strategy, all, 8))
        println("${links.size} links found")

        println("${all.size} points found")
//        val dupes = getDuplicatePaths(links)
//        println("${dupes.size} dupes found")
//        links.removeAll(dupes)

        val unlinked = getUnlinkedPoints(all, links)
        all.removeAll(unlinked)
        println("${unlinked.size} unlinked points removed")

        writeToFile("./points.dat", all, links)
        println("Done")
    }

    fun getFloodedTiles(
        traversal: TraversalStrategy,
        start: Tile,
        area: Area3D<Tile>
    ): Map<Tile, Int> {
        val distances = mutableMapOf<Tile, Int>()
        val queue = LinkedList<Tile>()
        queue.add(start)
        distances[start] = 0
        while (queue.isNotEmpty()) {
            val parent = queue.poll()
            for (direction in Direction.clockwise) {
                val tile = parent.add(direction.delta)
                if (!distances.containsKey(tile) && area.contains(tile) && !traversal.blocked(parent, direction)) {
                    distances[tile] = distances[parent]!! + 1
                    queue.addLast(tile)
                }
            }
        }
        return distances
    }

    fun euclidean(first: Tile, second: Tile): Double {
        return sqrt(((first.x - second.x) * (first.x - second.x) + (first.y - second.y) * (first.y - second.y)).toDouble())
    }

    fun centroid(tiles: Set<Tile>): Tile {
        var x = 0
        var y = 0
        for (tile in tiles) {
            x += tile.x
            y += tile.y
        }
        return Tile(x / tiles.size, y / tiles.size)
    }

    fun getCenterPoints(traversal: TraversalStrategy, area: Area3D<Tile>): List<Tile> {
        val list = mutableListOf<Tile>()
        val visitedTiles = mutableSetOf<Tile>()
        for (tile in area) {
            if (!visitedTiles.contains(tile) && !traversal.blocked(tile, Direction.NONE)) {
                val knots = getFloodedTiles(traversal, tile, area).keys
                if (knots.size > 2) {
                    var center = centroid(knots)
//                if (traversal.blocked(center, Direction.NONE)) {
                    center = knots.minBy { euclidean(it, center) } ?: continue
//                }
                    visitedTiles.addAll(knots)
                    list.add(center)
                }
            }
        }
        return list
    }

    fun getStaticLinks(
        traversal: TraversalStrategy,
        points: Set<Tile>,
        clusterSize: Int
    ): Set<Triple<Tile, Tile, Int>> {
        val map = mutableMapOf<Tile, MutableSet<Tile>>()
        val set = mutableSetOf<Triple<Tile, Tile, Int>>()
        val cluster = clusterSize * 3
        for (start in points) {
            val tiles = getFloodedTiles(
                traversal,
                start,
                start.chunk.tile.minus(clusterSize, clusterSize).area(width = cluster, height = cluster)
            )
            val visited = mutableSetOf<Tile>()
            for ((end, distance) in tiles) {
                if (start == end || visited.contains(end) || !points.contains(end) || distance > clusterSize * 2 /*|| outOfView(start, end)*/) {
                    continue
                }
                if (!set.contains(Triple(end, start, distance))) {
                    set.add(Triple(start, end, distance))
                    map.getOrPut(start) { mutableSetOf() }.add(end)
                    map.getOrPut(end) { mutableSetOf() }.add(start)
                    visited.add(end)
                    visited.addAll(map[end] ?: continue)
                }
            }
        }
        return set
    }

    fun outOfView(start: Tile, end: Tile) = abs(start.x - end.x) >= 15 || abs(start.y - end.y) >= 15

    fun getUnlinkedPoints(
        points: Set<Tile>,
        links: Set<Triple<Tile, Tile, Int>>
    ): Set<Tile> {
        val combined = links.map { it.first }.toMutableSet()
        combined.addAll(links.map { it.second })
        val unreachable = mutableSetOf<Tile>()
        for (point in points) {
            if (combined.contains(point)) {
                continue
            }
            unreachable.add(point)
        }
        return unreachable
    }

    fun writeToFile(path: String, points: Set<Tile>, links: Set<Triple<Tile, Tile, Int>>) {
        val file = File(path)
        val stream = DataOutputStream(file.outputStream())
        stream.writeInt(points.size)
        stream.writeInt(links.size)
        for (point in points) {
            stream.writeInt(point.id)
        }
        for (link in links) {
            stream.writeInt(link.first.id)
            stream.writeInt(link.second.id)
            stream.writeInt(link.third)
        }
        stream.close()
    }

    fun getPortals(objects: Set<GameObject>): Set<Pair<Tile, Tile>> {
        val portals = mutableSetOf<Pair<Tile, Tile>>()
        for (gameObject in objects) {
            if (gameObject.def.isDoor() && gameObject.def.options?.any { it?.contains("open", true) == true } == true) {
                val dir = Direction.cardinal[(gameObject.rotation + 3) and 0x3]
                portals.add(gameObject.tile to gameObject.tile.add(dir.delta))
            }
        }
        return portals
    }


    /*
         e
         | \
         |  \
     d---a-/-b
     */
    fun getDuplicatePaths(links: Set<Triple<Tile, Tile, Int>>): Set<Triple<Tile, Tile, Int>> {
        val duplicates = mutableSetOf<Triple<Tile, Tile, Int>>()
        val map = mutableMapOf<Tile, MutableSet<Tile>>()
        links.forEach {
            map.getOrPut(it.first) { mutableSetOf() }.add(it.second)
            map.getOrPut(it.second) { mutableSetOf() }.add(it.first)
        }

        for (link in links) {
            val source = link.first
            val sourceTargets = map[source] ?: continue
            val target = link.second
            val targetTargets = map[target] ?: continue
            val intersect = sourceTargets.intersect(targetTargets)
            if(intersect.isNotEmpty()) {
                duplicates.add(link)
                sourceTargets.remove(target)
                targetTargets.remove(source)
            }
        }
        return duplicates
    }
}