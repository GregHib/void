package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check

class ObjectLinker(private val collisions: Collisions) {
    fun deltaBetween(one: GameObject, two: GameObject): Delta? {
        val pair = linkedPoints(one, two) ?: return null
        return pair.second.delta(pair.first)
    }

    fun linkedPoints(one: GameObject, two: GameObject): Pair<Tile, Tile>? {
        val ones = getAvailableTiles(one)
        if (ones.isEmpty()) {
            return null
        }
        val twos = getAvailableTiles(two)
        if (twos.isEmpty()) {
            return null
        }
        if (ones.size == 1 && twos.size == 1) {
            return ones.first() to twos.first()
        }
        var shortest = Int.MAX_VALUE
        var pair: Pair<Tile, Tile>? = null
        for (first in ones) {
            val firstDelta = one.tile.minus(first)
            for (second in twos) {
                val secondDelta = two.tile.minus(second)
                val dist = firstDelta.distanceTo(secondDelta)
                if (dist < shortest) {
                    shortest = dist
                    pair = first to second
                }
            }
        }
        return pair
    }

    /**
     * Returns a walkable tile within radius of 1
     */
    fun getValidTile(tile: Tile): Tile? {
        if (!collisions.check(tile.x, tile.y, tile.plane, CollisionFlag.BLOCKED)) {
            return tile
        }
        Direction.all.forEach {
            val tile = tile.add(it.delta)
            if (!collisions.check(tile.x, tile.y, tile.plane, CollisionFlag.BLOCKED)) {
                return tile
            }
        }
        return null
    }

    fun getAvailableTiles(obj: GameObject): List<Tile> {
        val list = mutableListOf<Tile>()
        for (dir in Direction.values) {
            val tile = getSizedTile(obj, dir)

            when (dir) {
                Direction.WEST, Direction.EAST -> {
                    for (y in 0 until obj.size.height) {
                        if (obj.reachableFrom(tile.addY(y))) {
                            list.add(tile.addY(y))
                        }
                    }
                }
                Direction.SOUTH, Direction.NORTH -> {
                    for (x in 0 until obj.size.width) {
                        if (obj.reachableFrom(tile.addX(x))) {
                            list.add(tile.addX(x))
                        }
                    }
                }
                else -> if (obj.reachableFrom(tile)) {
                    list.add(tile)
                }
            }
        }
        return list
    }

    fun getTiles(obj: GameObject): Set<Tile> {
        val list = mutableSetOf<Tile>()
        for (dir in Direction.values) {
            val tile = getSizedTile(obj, dir)

            when (dir) {
                Direction.WEST, Direction.EAST -> {
                    for (y in 0 until obj.size.height) {
                        if (obj.reachableFrom(tile.addY(y))) {
                            list.add(tile.addY(y))
                        }
                    }
                }
                Direction.SOUTH, Direction.NORTH -> {
                    for (x in 0 until obj.size.width) {
                        if (obj.reachableFrom(tile.addX(x))) {
                            list.add(tile.addX(x))
                        }
                    }
                }
                else -> if (obj.reachableFrom(tile)) {
                    list.add(tile)
                }
            }
        }
        return list
    }

    fun getAllTiles(obj: GameObject): List<Tile> {
        val list = mutableListOf<Tile>()
        for (plane in 0 until 4) {
            for (dir in Direction.values) {
                val tile = getSizedTile(obj, dir).copy(plane = plane)

                when (dir) {
                    Direction.WEST, Direction.EAST -> {
                        for (y in 0 until obj.size.height) {
                            if (obj.reachableFrom(tile.addY(y))) {
                                list.add(tile.addY(y))
                            }
                        }
                    }
                    Direction.SOUTH, Direction.NORTH -> {
                        for (x in 0 until obj.size.width) {
                            if (obj.reachableFrom(tile.addX(x))) {
                                list.add(tile.addX(x))
                            }
                        }
                    }
                    else -> if (obj.reachableFrom(tile)) {
                        list.add(tile)
                    }
                }
            }
        }
        return list
    }

    fun isReachable(obj: GameObject): Boolean {
        for (dir in Direction.values()) {
            val tile = getSizedTile(obj, dir)

            when (dir) {
                Direction.WEST, Direction.EAST -> {
                    for (y in 0 until obj.size.height) {
                        if (obj.reachableFrom(tile.addY(y))) {
                            return true
                        }
                    }
                }
                Direction.SOUTH, Direction.NORTH -> {
                    for (x in 0 until obj.size.width) {
                        if (obj.reachableFrom(tile.addX(x))) {
                            return true
                        }
                    }
                }
                else -> if (obj.reachableFrom(tile)) {
                    return true
                }
            }
        }
        return false
    }

    private fun GameObject.reachableFrom(tile: Tile): Boolean {
        return interactTarget.reached(tile, Size.ONE) && !collisions.check(tile.x, tile.y, tile.plane, CollisionFlag.BLOCKED)
    }

    private fun getSizedTile(obj: GameObject, dir: Direction): Tile {
        var tile = obj.tile.add(dir.delta)
        if (dir.horizontal() == Direction.EAST) {
            tile = tile.addX(obj.size.width - 1)
        }
        if (dir.vertical() == Direction.NORTH) {
            tile = tile.addY(obj.size.height - 1)
        }
        return tile
    }
}