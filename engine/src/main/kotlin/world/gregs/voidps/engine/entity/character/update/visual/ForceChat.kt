package world.gregs.voidps.engine.entity.character.update.visual

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.visual.VisualMask.NPC_FORCE_CHAT_MASK
import world.gregs.voidps.network.visual.VisualMask.PLAYER_FORCE_CHAT_MASK

private fun mask(character: Character) = if (character is Player) PLAYER_FORCE_CHAT_MASK else NPC_FORCE_CHAT_MASK

fun Character.flagForceChat() = visuals.flag(mask(this))

var Character.forceChat: String
    get() = visuals.forceChat.text
    set(value) {
        visuals.forceChat.text = value
        flagForceChat()
    }