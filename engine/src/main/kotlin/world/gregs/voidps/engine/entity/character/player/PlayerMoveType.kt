package world.gregs.voidps.engine.entity.character.player

enum class PlayerMoveType(val id: Int) {
    None(0),
    Walk(1),
    Run(2),
    Teleport(127)
}