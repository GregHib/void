package world.gregs.voidps.world.interact.dialogue.event

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerEvent
import world.gregs.voidps.engine.event.EventCompanion

data class ContinueDialogue(
    override val player: Player,
    val id: Int,
    val name: String,
    val componentId: Int,
    val component: String,
    val type: String,
    val option: Int
) : PlayerEvent() {
    companion object : EventCompanion<ContinueDialogue>
}