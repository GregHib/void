package world.gregs.voidps.engine.entity.character.move

import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.map.Tile

data class PlayerMoved(val from: Tile, val to: Tile) : PlayerEvent