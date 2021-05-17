package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.Visual

data class ForceChat(var text: String = "") : Visual {
    override fun needsReset(character: Character): Boolean {
        return text.isNotEmpty()
    }

    override fun reset(character: Character) {
        text = ""
    }
}

const val PLAYER_FORCE_CHAT_MASK = 0x1000

const val NPC_FORCE_CHAT_MASK = 0x1

private fun mask(character: Character) = if (character is Player) PLAYER_FORCE_CHAT_MASK else NPC_FORCE_CHAT_MASK

fun Character.flagForceChat() = visuals.flag(mask(this))

fun Character.getForceChat() = visuals.getOrPut(mask(this)) { ForceChat() }

var Character.forceChat: String
    get() = getForceChat().text
    set(value) {
        getForceChat().text = value
        flagForceChat()
    }