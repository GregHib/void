package rs.dusk.engine.model.entity.index.update.visual

import rs.dusk.engine.model.entity.index.Indexed
import rs.dusk.engine.model.entity.index.npc.NPC
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.engine.model.entity.index.update.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class ForceChat(var text: String = "") : Visual {
    override fun reset(indexed: Indexed) {
        text = ""
    }
}

const val PLAYER_FORCE_CHAT_MASK = 0x4000

const val NPC_FORCE_CHAT_MASK = 0x2

fun Player.flagForceChat() = visuals.flag(PLAYER_FORCE_CHAT_MASK)

fun NPC.flagForceChat() = visuals.flag(NPC_FORCE_CHAT_MASK)

fun Player.getForceChat() = visuals.getOrPut(PLAYER_FORCE_CHAT_MASK) { ForceChat() }

fun NPC.getForceChat() = visuals.getOrPut(NPC_FORCE_CHAT_MASK) { ForceChat() }

var Player.forceChat: String
    get() = getForceChat().text
    set(value) {
        getForceChat().text = value
        flagForceChat()
    }

var NPC.forceChat: String
    get() = getForceChat().text
    set(value) {
        getForceChat().text = value
        flagForceChat()
    }