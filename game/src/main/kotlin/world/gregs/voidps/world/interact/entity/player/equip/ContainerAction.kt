package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.entity.character.player.PlayerEvent

data class ContainerAction(
    val container: String,
    val item: String,
    val slot: Int,
    val option: String
) : PlayerEvent