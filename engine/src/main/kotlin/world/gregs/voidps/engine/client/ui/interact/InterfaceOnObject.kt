package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject

data class InterfaceOnObject(
    override val player: Player,
    val obj: GameObject,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val container: String
) : Interaction()