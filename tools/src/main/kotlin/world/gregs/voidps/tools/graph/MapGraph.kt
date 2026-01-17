package world.gregs.voidps.tools.graph

import content.entity.obj.door.Door.isDoor
import kotlinx.coroutines.runBlocking
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.tools.cache.Xteas
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Distance
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Cuboid
import java.io.DataOutputStream
import java.io.File
import java.util.*
import kotlin.math.sqrt
import kotlin.system.measureNanoTime

class MapGraph(
    private val objects: GameObjects,
    private val xteas: Xteas,
    private val cache: Cache,
) {

    fun load(regionId: Int) {
//        216, 320 - 487, 504
        val all = mutableSetOf<Tile>()
        val objs = mutableSetOf<GameObject>()
        val links = mutableSetOf<Triple<Tile, Tile, Int>>()
        val strategy = SmallTraversal

        val reg = Region(27, 40).toLevel(0)
        runBlocking {
            for (region in reg.toCuboid(width = 33, height = 23).toRegions()) {
                // TODO better way of determining empty maps
                val xtea = xteas[region.id]
                cache.data(5, "l${region.x}_${region.y}", xtea) ?: continue

                for (zone in region.tile.zone.toCuboid(width = 8, height = 8).toZones()) {
                    val time = measureNanoTime {
                        val loaded = zone.toCuboid().flatMap { tile -> objects[tile] }
                        objs.addAll(loaded)
                        all.addAll(getCenterPoints(strategy, zone.toCuboid(width = 2, height = 2)))
                    }
                    println("Objects ${objs.size} Points ${all.size} Took ${time}ns")
                }
            }
        }

        val portals = getPortals(objs)
        println("${portals.size} portals found")
        for (portal in portals) {
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
        traversal: TileTraversalStrategy,
        start: Tile,
        area: Cuboid,
    ): Map<Tile, Int> {
        val distances = mutableMapOf<Tile, Int>()
        val queue = LinkedList<Tile>()
        queue.add(start)
        distances[start] = 0
        while (queue.isNotEmpty()) {
            val parent = queue.poll()
            for (direction in Direction.clockwise) {
                val tile = parent.add(direction.delta)
                if (!distances.containsKey(tile) && area.contains(tile) && !traversal.blocked(parent, 1, direction)) {
                    distances[tile] = distances[parent]!! + 1
                    queue.addLast(tile)
                }
            }
        }
        return distances
    }

    fun euclidean(first: Tile, second: Tile): Double = sqrt(((first.x - second.x) * (first.x - second.x) + (first.y - second.y) * (first.y - second.y)).toDouble())

    fun centroid(tiles: Set<Tile>): Tile {
        var x = 0
        var y = 0
        for (tile in tiles) {
            x += tile.x
            y += tile.y
        }
        return Tile(x / tiles.size, y / tiles.size)
    }

    fun getCenterPoints(traversal: TileTraversalStrategy, area: Cuboid): List<Tile> {
        val list = mutableListOf<Tile>()
        val visitedTiles = mutableSetOf<Tile>()
        for (tile in area) {
            if (!visitedTiles.contains(tile) && !traversal.blocked(tile, 1, Direction.NONE)) {
                val knots = getFloodedTiles(traversal, tile, area).keys
                if (knots.size > 2) {
                    var center = centroid(knots)
//                if (traversal.blocked(center, Direction.NONE)) {
                    center = knots.minByOrNull { euclidean(it, center) } ?: continue
//                }
                    visitedTiles.addAll(knots)
                    list.add(center)
                }
            }
        }
        return list
    }

    fun getStaticLinks(
        traversal: TileTraversalStrategy,
        points: Set<Tile>,
        clusterSize: Int,
    ): Set<Triple<Tile, Tile, Int>> {
        val map = mutableMapOf<Tile, MutableSet<Tile>>()
        val set = mutableSetOf<Triple<Tile, Tile, Int>>()
        val cluster = clusterSize * 3
        for (start in points) {
            val tiles = getFloodedTiles(
                traversal,
                start,
                start.zone.tile.minus(clusterSize, clusterSize).toCuboid(width = cluster, height = cluster),
            )
            val visited = mutableSetOf<Tile>()
            for ((end, distance) in tiles) {
                if (start == end || visited.contains(end) || !points.contains(end) || distance > clusterSize * 2) { // || outOfView(start, end)
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

    fun outOfView(start: Tile, end: Tile) = Distance.within(start.x, start.y, end.x, end.y, 15)

    fun getUnlinkedPoints(
        points: Set<Tile>,
        links: Set<Triple<Tile, Tile, Int>>,
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

    private fun writeToFile(path: String, points: Set<Tile>, links: Set<Triple<Tile, Tile, Int>>) {
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
            if (intersect.isNotEmpty()) {
                duplicates.add(link)
                sourceTargets.remove(target)
                targetTargets.remove(source)
            }
        }
        return duplicates
    }
}
