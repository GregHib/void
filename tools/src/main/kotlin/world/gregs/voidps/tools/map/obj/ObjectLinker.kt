package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.check
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class ObjectLinker {
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
        if (!Collisions.check(tile.x, tile.y, tile.level, 0x100)) { // BLOCKED
            return tile
        }
        Direction.all.forEach {
            val tile = tile.add(it.delta)
            if (!Collisions.check(tile.x, tile.y, tile.level, 0x100)) { // BLOCKED
                return tile
            }
        }
        return null
    }

    fun getAvailableTiles(obj: GameObject, level: Int = obj.tile.level, list: MutableList<Tile> = mutableListOf()): List<Tile> {
        for (dir in Direction.values) {
            val tile = getSizedTile(obj, dir).copy(level = level)
            when (dir) {
                Direction.WEST, Direction.EAST -> {
                    for (y in 0 until obj.height) {
                        if (obj.reachableFrom(tile.addY(y))) {
                            list.add(tile.addY(y))
                        }
                    }
                }
                Direction.SOUTH, Direction.NORTH -> {
                    for (x in 0 until obj.width) {
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
        for (level in 0 until 4) {
            getAvailableTiles(obj, level, list)
        }
        return list
    }

    fun isReachable(obj: GameObject): Boolean {
        for (dir in Direction.entries) {
            val tile = getSizedTile(obj, dir)

            when (dir) {
                Direction.WEST, Direction.EAST -> {
                    for (y in 0 until obj.height) {
                        if (obj.reachableFrom(tile.addY(y))) {
                            return true
                        }
                    }
                }
                Direction.SOUTH, Direction.NORTH -> {
                    for (x in 0 until obj.width) {
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
        return false // interactTarget.reached(tile, Size.ONE) && !collisions.check(tile.x, tile.y, tile.level, 0x100) // BLOCKED
    }

    private fun getSizedTile(obj: GameObject, dir: Direction): Tile {
        var tile = obj.tile.add(dir.delta)
        if (dir.horizontal() == Direction.EAST) {
            tile = tile.addX(obj.width - 1)
        }
        if (dir.vertical() == Direction.NORTH) {
            tile = tile.addY(obj.height - 1)
        }
        return tile
    }
}
