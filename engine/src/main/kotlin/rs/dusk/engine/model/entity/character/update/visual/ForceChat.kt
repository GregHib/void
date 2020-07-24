package rs.dusk.engine.model.entity.character.update.visual

import rs.dusk.engine.model.entity.character.Character
import rs.dusk.engine.model.entity.character.npc.NPC
import rs.dusk.engine.model.entity.character.npc.NPCEvent
import rs.dusk.engine.model.entity.character.player.Player
import rs.dusk.engine.model.entity.character.player.PlayerEvent
import rs.dusk.engine.model.entity.character.update.Visual

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
data class ForceChat(var text: String = "") : Visual {
    override fun reset(character: Character) {
        text = ""
    }
}

const val PLAYER_FORCE_CHAT_MASK = 0x4000

const val NPC_FORCE_CHAT_MASK = 0x2

fun Player.flagForceChat() = visuals.flag(PLAYER_FORCE_CHAT_MASK)

fun NPC.flagForceChat() = visuals.flag(NPC_FORCE_CHAT_MASK)

fun Player.getForceChat() = visuals.getOrPut(PLAYER_FORCE_CHAT_MASK) { ForceChat() }

fun NPC.getForceChat() = visuals.getOrPut(NPC_FORCE_CHAT_MASK) { ForceChat() }

fun PlayerEvent.force(chatText: String) {
    player.forceChat = chatText
}

fun NPCEvent.force(chatText: String) {
    npc.forceChat = chatText
}

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