package world.gregs.voidps.engine.entity.item.floor

data class ItemSpawn(
    val id: String,
    val amount: Int = 1,
    val delay: Int = 60
)