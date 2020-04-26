package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.Indexed
import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class ForceChat(var text: String = "") : Visual

fun Player.flagForceChat() = visuals.flag(0x4000)

fun NPC.flagForceChat() = visuals.flag(0x2)

fun Indexed.flagForceChat() {
    if (this is Player) flagForceChat() else if (this is NPC) flagForceChat()
}

fun Indexed.getForceChat() = visuals.getOrPut(ForceChat::class) { ForceChat() }

fun Indexed.setForceChat(text: String = "") {
    val chat = getForceChat()
    chat.text = text
    flagForceChat()
}
