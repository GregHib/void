package rs.dusk.world.interact.dialogue

import rs.dusk.engine.client.ui.dialogue.Dialogues
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.engine.entity.character.player.PlayerEvent
import rs.dusk.engine.event.EventCompanion

data class ContinueDialogue(
    override val player: Player,
    val id: Int,
    val name: String,
    val componentId: Int,
    val component: String,
    val type: Dialogues.Type,
    val option: Int
) : PlayerEvent() {
    companion object : EventCompanion<ContinueDialogue>
}