package rs.dusk.engine.model.entity.character.player

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since May 15, 2020
 */
sealed class PlayerMoveType(val id: Int) {
    object None : PlayerMoveType(0)
    object Walk : PlayerMoveType(1)
    object Run : PlayerMoveType(2)
    object Teleport : PlayerMoveType(127)
}