package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual

data class ForceChat(var text: String = "") : Visual {
    override fun needsReset(): Boolean {
        return text.isNotEmpty()
    }

    override fun reset() {
        text = ""
    }
}

const val PLAYER_FORCE_CHAT_MASK = 0x1000

const val NPC_FORCE_CHAT_MASK = 0x1

private fun mask(character: Character) = if (character is Player) PLAYER_FORCE_CHAT_MASK else NPC_FORCE_CHAT_MASK

fun Character.flagForceChat() = visuals.flag(mask(this))

var Character.forceChat: String
    get() = visuals.forceChat.text
    set(value) {
        visuals.forceChat.text = value
        flagForceChat()
    }