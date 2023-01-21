package world.gregs.voidps.world.interact.entity.player.equip

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

data class ContainerOption(
    override val player: Player,
    val container: String,
    val item: Item,
    val slot: Int,
    val option: String
) : Interaction()