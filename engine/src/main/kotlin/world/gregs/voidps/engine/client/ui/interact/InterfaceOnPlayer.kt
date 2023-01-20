package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

data class InterfaceOnPlayer(
    override val player: Player,
    val target: Player,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val container: String
) : Interaction()