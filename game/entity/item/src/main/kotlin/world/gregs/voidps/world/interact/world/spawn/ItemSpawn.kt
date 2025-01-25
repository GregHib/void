package world.gregs.voidps.world.interact.world.spawn

data class ItemSpawn(
    val id: String,
    val amount: Int = 1,
    val delay: Int = 60
)