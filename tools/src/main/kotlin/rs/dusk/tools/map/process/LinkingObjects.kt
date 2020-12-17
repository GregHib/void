package rs.dusk.tools.map.process

import rs.dusk.engine.entity.Direction
import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.engine.map.Tile
import rs.dusk.engine.map.collision.CollisionFlag
import rs.dusk.engine.map.collision.Collisions
import rs.dusk.engine.map.collision.check
import rs.dusk.engine.map.collision.get

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
        if(ones.size == 1 && twos.size == 1) {
            return twos.first().delta(ones.first())
        }
        var smallest = Int.MAX_VALUE
        for(one in ones) {
            for(two in twos) {
//                if(one)
            }
        }
        if (ones.isNotEmpty() && twos.isNotEmpty()) {
            return twos.first().delta(ones.first())
        }
        // Directly above
//        for (dir in Direction.all) {
//            if (one.reachableFrom(one.tile.add(dir.delta)) && two.reachableFrom(two.tile.add(dir.delta))) {
//                return two.tile.add(dir.delta).delta(one.tile.add(dir.delta))
//            }
//        }
//        // Opposite directions
//        for (dir in Direction.values()) {
//            val tile = getSizedTile(one, dir)
//            when (dir) {
//                Direction.WEST, Direction.EAST -> {
//                    for (y in 0 until one.size.height) {
//                        val t = check(one, two, tile, 0, y, dir)
//                        if (t != null) {
//                            return t
//                        }
//                    }
//                }
//                Direction.SOUTH, Direction.NORTH -> {
//                    for (x in 0 until one.size.width) {
//                        val t = check(one, two, tile, x, 0, dir)
//                        if (t != null) {
//                            return t
//                        }
//                    }
//                }
//                else -> {
//                    val t = check(one, two, tile, 0, 0, dir)
//                    if (t != null) {
//                        return t
//                    }
//                }
//            }
//        }
//        // Any directions
//        val ones = getAvailableTiles(one)
//        val twos = getAvailableTiles(two)
//        if (ones.isNotEmpty() && twos.isNotEmpty()) {
//            return twos.first().delta(ones.first())
//        }
        return null
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

    private fun check(one: GameObject, two: GameObject, tile: Tile, x: Int, y: Int, dir: Direction): Tile? {
        val point = tile.add(x = x, y = y)
        if (one.reachableFrom(point)) {
            println("Is walkable $point ${collisions.get(tile.x, tile.y, tile.plane)}")
            val opposite = dir.inverse()
            val other = getSizedTile(two, opposite).add(x = x, y = y)
            println("Other? $opposite $other ${two.reachableFrom(other)}")
            if (two.reachableFrom(other)) {
                return other.delta(point)
            }
        }
        return null
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