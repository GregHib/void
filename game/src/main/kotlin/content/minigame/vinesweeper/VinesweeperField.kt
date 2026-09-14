package content.minigame.vinesweeper

import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Shared state of the Vinesweeper field: which holes hide a seed and who planted each flag.
 * The board is global, every player digs the same field.
 */
object VinesweeperField {

    private val seeds = HashSet<Tile>()
    private val flagOwners = HashMap<Tile, String>()

    val area: Area
        get() = Areas["vinesweeper_field"]

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

    fun hole(tile: Tile): GameObject? = GameObjects.at(tile).firstOrNull { it.id.startsWith("vinesweeper_hole") }

    fun objectAt(tile: Tile, prefix: String): GameObject? = GameObjects.at(tile).firstOrNull { it.id.startsWith(prefix) }

    fun holes(): List<GameObject> {
        val list = mutableListOf<GameObject>()
        val area = area
        for (tile in area) {
            val hole = hole(tile) ?: continue
            list.add(hole)
        }
        return list
    }

    /**
     * Top the field up with hidden seeds until [SEED_PERCENT] of the holes are seeded.
     */
    fun populate() {
        val holes = holes()
        val target = minOf(MAX_SEEDS, holes.size * SEED_PERCENT / 100)
        val candidates = holes.map { it.tile }.filter { !isSeed(it) }.toMutableList()
        while (seeds.size < target && candidates.isNotEmpty()) {
            val tile = candidates.removeAt(random.nextInt(candidates.size))
            plant(tile)
        }
    }

    private const val SEED_PERCENT = 13
    private const val MAX_SEEDS = 300
}
