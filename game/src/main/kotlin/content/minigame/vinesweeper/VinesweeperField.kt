package content.minigame.vinesweeper

import content.minigame.vinesweeper.VinesweeperField.SEED_PERCENT
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Shared state of the Vinesweeper field: which holes hide a seed and who planted each flag.
 * The board is global, every player digs the same field.
 */
object VinesweeperField {

    private val seeds = HashSet<Tile>()
    private val flagOwners = HashMap<Tile, String>()

    val seedCount: Int
        get() = seeds.size

    fun reset() {
        seeds.clear()
        flagOwners.clear()
    }

    fun isSeed(tile: Tile): Boolean = seeds.contains(tile)

    fun plant(tile: Tile) {
        seeds.add(tile)
    }

    fun remove(tile: Tile) {
        seeds.remove(tile)
    }

    fun flag(tile: Tile, owner: String) {
        flagOwners[tile] = owner
    }

    fun flagOwner(tile: Tile): String? = flagOwners[tile]

    fun unflag(tile: Tile) {
        flagOwners.remove(tile)
    }

    fun adjacentSeeds(tile: Tile): Int {
        var count = 0
        for (x in -1..1) {
            for (y in -1..1) {
                if ((x != 0 || y != 0) && isSeed(tile.add(x, y))) {
                    count++
                }
            }
        }
        return count
    }

    fun hole(tile: Tile): GameObject? {
        val obj = GameObjects.getLayer(tile, ObjectLayer.GROUND)
        if (obj != null && obj.id.startsWith("vinesweeper_hole")) {
            return obj
        }
        return null
    }

    fun objectAt(tile: Tile, prefix: String): GameObject? {
        val obj = GameObjects.getLayer(tile, ObjectLayer.GROUND)
        if (obj != null && obj.id.startsWith(prefix)) {
            return obj
        }
        return null
    }

    fun seedlessHoles(): MutableList<Tile> {
        val list = mutableListOf<Tile>()
        for (tile in Areas["vinesweeper_field"]) {
            hole(tile) ?: continue
            if (isSeed(tile)) {
                continue
            }
            list.add(tile)
        }
        return list
    }

    /**
     * Top the field up with hidden seeds until [SEED_PERCENT] of the holes are seeded.
     */
    fun populate() {
        val holes = seedlessHoles()
        val target = minOf(MAX_SEEDS, holes.size * SEED_PERCENT / 100)
        while (seeds.size < target && holes.isNotEmpty()) {
            val index = random.nextInt(holes.size)
            if (index !in holes.indices) {
                break
            }
            val tile = holes.removeAt(index)
            plant(tile)
        }
    }

    private const val SEED_PERCENT = 13
    private const val MAX_SEEDS = 300
}
