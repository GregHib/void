package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.mode.interact.NPCTargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

data class ItemOnNPC(
    override val player: Player,
    override val target: NPC,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : NPCTargetInteraction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}