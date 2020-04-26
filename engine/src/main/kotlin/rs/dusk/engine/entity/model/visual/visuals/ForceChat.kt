package rs.dusk.engine.entity.model.visual.visuals

import rs.dusk.engine.entity.model.NPC
import rs.dusk.engine.entity.model.Player
import rs.dusk.engine.entity.model.visual.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class ForceChat(var text: String = "") : Visual

const val PLAYER_FORCE_CHAT_MASK = 0x4000

const val NPC_FORCE_CHAT_MASK = 0x2

fun Player.flagForceChat() = visuals.flag(PLAYER_FORCE_CHAT_MASK)

fun NPC.flagForceChat() = visuals.flag(NPC_FORCE_CHAT_MASK)

fun Player.getForceChat() = visuals.getOrPut(PLAYER_FORCE_CHAT_MASK) { ForceChat() }

fun NPC.getForceChat() = visuals.getOrPut(NPC_FORCE_CHAT_MASK) { ForceChat() }

fun Player.setForceChat(text: String = "") {
    getForceChat().text = text
    flagForceChat()
}

fun NPC.setForceChat(text: String = "") {
    getForceChat().text = text
    flagForceChat()
}
