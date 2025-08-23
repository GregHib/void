package world.gregs.voidps.engine.event

data class UseData(
    val sources: MutableSet<String> = mutableSetOf(),
    var targets: MutableList<String> = mutableListOf(),
    var option: String = "*",
    var component: String = "*",
    var approach: Boolean = false,
    var flag: Int = 0,
)