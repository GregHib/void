package world.gregs.voidps.engine.entity.item

import world.gregs.voidps.engine.entity.character.player.PlayerEvent

data class FloorItemOption(val floorItem: FloorItem, val option: String?, val partial: Boolean) : PlayerEvent