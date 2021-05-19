package world.gregs.voidps.engine.entity.character.player

sealed class PlayerMoveType(val id: Int) {
    object None : PlayerMoveType(0)
    object Walk : PlayerMoveType(1)
    object Run : PlayerMoveType(2)
    object Teleport : PlayerMoveType(127)
}