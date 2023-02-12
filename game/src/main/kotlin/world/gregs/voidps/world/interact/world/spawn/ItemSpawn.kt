package world.gregs.voidps.world.interact.world.spawn

import world.gregs.voidps.engine.map.Tile

data class ItemSpawn(
    val id: String,
    val tile: Tile,
    val amount: Int,
    val delay: Int,
    val members: Boolean = false
) {

    companion object {
        fun fromMap(it: Map<String, Any>) = ItemSpawn(
            id = it["id"] as String,
            tile = Tile.fromMap(it),
            amount = it["amount"] as? Int ?: 1,
            delay = it["delay"] as? Int ?: 60,
            members = it["members"] as? Boolean ?: false,
        )
    }
}