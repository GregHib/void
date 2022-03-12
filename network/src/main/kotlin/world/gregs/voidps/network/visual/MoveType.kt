package world.gregs.voidps.network.visual

enum class MoveType(val id: Int) {
    None(0),
    Walk(1),
    Run(2),
    Teleport(127)
}