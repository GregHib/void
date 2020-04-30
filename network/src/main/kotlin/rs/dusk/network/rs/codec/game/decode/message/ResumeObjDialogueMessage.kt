package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Called by script 580 - G.E item search clearing
 * @param value Unknown value
 */
data class ResumeObjDialogueMessage(val value: Int) : Message {
    companion object : MessageCompanion<ResumeObjDialogueMessage>()
}