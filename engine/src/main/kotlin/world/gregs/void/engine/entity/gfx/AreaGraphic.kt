package world.gregs.void.engine.entity.gfx

import world.gregs.void.engine.entity.Entity
import world.gregs.void.engine.entity.character.player.Player
import world.gregs.void.engine.entity.character.update.visual.Graphic
import world.gregs.void.engine.entity.character.update.visual.player.name
import world.gregs.void.engine.map.Tile

data class AreaGraphic(override var tile: Tile, val graphic: Graphic, val owner: String? = null) : Entity {
    override val id = -1

    fun visible(player: Player) = owner == null || owner == player.name
}