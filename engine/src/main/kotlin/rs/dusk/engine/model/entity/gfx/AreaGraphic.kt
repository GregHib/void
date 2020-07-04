package rs.dusk.engine.model.entity.gfx

import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.update.visual.Graphic
import rs.dusk.engine.model.entity.index.update.visual.player.name
import rs.dusk.engine.model.world.Tile

data class AreaGraphic(override var tile: Tile, val graphic: Graphic, val owner: String? = null) : Entity {
    override val id = -1

    fun visible(player: Player) = owner == null || owner == player.name
}