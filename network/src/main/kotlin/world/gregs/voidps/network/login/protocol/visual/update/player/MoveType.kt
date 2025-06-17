package world.gregs.voidps.network.login.protocol.visual.update.player

enum class MoveType(val id: Int) {
    None(0),
    Walk(1),
    Run(2),
    Teleport(127),
}
