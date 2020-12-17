package rs.dusk.tools.map.process

import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.collision.CollisionFlag
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.map.collision.check

class LinkingObjects(private val collisions: Collisions) {
    fun deltaBetween(one: GameObject, two: GameObject): Tile? {
        val ones = getAvailableTiles(one)
        if (ones.isEmpty()) {
            return null
        }
        val twos = getAvailableTiles(two)
        if (twos.isEmpty()) {
            return null
        }
        if (ones.size == 1 && twos.size == 1) {
            return twos.first().delta(ones.first())
        }
        var shortest = Int.MAX_VALUE
        var delta: Tile? = null
        for (first in ones) {
            for (second in twos) {
                val dist = first.distanceTo(second)
                if (dist < shortest) {
                    shortest = dist
                    delta = second.delta(first)
                }
            }
        }
        return delta
    }

    private fun getAvailableTiles(one: GameObject): List<Tile> {
        val list = mutableListOf<Tile>()
        for (dir in Direction.values()) {
            val tile = getSizedTile(one, dir)

            when (dir) {
                Direction.WEST, Direction.EAST -> {
                    for (y in 0 until one.size.height) {
                        if (one.reachableFrom(tile.add(y = y))) {
                            list.add(tile.add(y = y))
                        }
                    }
                }
                Direction.SOUTH, Direction.NORTH -> {
                    for (x in 0 until one.size.width) {
                        if (one.reachableFrom(tile.add(x = x))) {
                            list.add(tile.add(x = x))
                        }
                    }
                }
                else -> if (one.reachableFrom(tile)) {
                    list.add(tile)
                }
            }
        }
        return list
    }

    private fun GameObject.reachableFrom(tile: Tile): Boolean {
        return interactTarget.reached(tile, Size.TILE) && !collisions.check(tile.x, tile.y, tile.plane, CollisionFlag.BLOCKED)
    }

    private fun getSizedTile(obj: GameObject, dir: Direction): Tile {
        var tile = obj.tile.add(dir.delta)
        if (dir.horizontal() == Direction.EAST) {
            tile = tile.add(x = obj.size.width - 1)
        }
        if (dir.vertical() == Direction.NORTH) {
            tile = tile.add(y = obj.size.height - 1)
        }
        return tile
    }
}