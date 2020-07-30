package rs.dusk.engine.model.entity.gfx

import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.update.visual.Graphic
import rs.dusk.engine.model.entity.character.update.visual.player.name
import rs.dusk.engine.model.map.Tile

data class AreaGraphic(override var tile: Tile, val graphic: Graphic, val owner: String? = null) : Entity {
    override val id = -1

    fun visible(player: Player) = owner == null || owner == player.name
}