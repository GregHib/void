package world.gregs.voidps.world.interact.world.spawn

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.timedLoad

class ItemSpawns(
    private val chunks: MutableMap<Int, ItemSpawn> = Int2ObjectOpenHashMap()
) {
    fun set(tile: Tile, spawn: ItemSpawn) {
        chunks[tile.id] = spawn
    }

    fun get(tile: Tile): ItemSpawn? = chunks[tile.id]

    fun clear() {
        chunks.clear()
    }
}

private data class ItemSpawnData(
    val id: String,
    val x: Int,
    val y: Int,
    val plane: Int = 0,
    val amount: Int = 1,
    val delay: Int = 60,
    val members: Boolean = false
)

fun loadItemSpawns(items: FloorItems, spawns: ItemSpawns, storage: FileStorage = get(), path: String = getProperty("itemSpawnsPath")) {
    timedLoad("item spawn") {
        spawns.clear()
        var count = 0
        val data: List<ItemSpawnData> = storage.loadType(path)
        val membersWorld = World.members
        for (item in data) {
            if (!membersWorld && item.members) {
                continue
            }
            val tile = Tile(item.x, item.y, item.plane)
            spawns.set(tile, ItemSpawn(item.id, item.amount, item.delay))
            items.add(item.id, item.amount, tile)
            count++
        }
        count
    }
}