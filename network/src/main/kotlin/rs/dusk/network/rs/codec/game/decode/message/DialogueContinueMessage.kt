package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * Notification that the "Click here to continue" button was pressed on a dialogue
 * @param hash The interface and component id combined
 * @param index The index of the interface select
 */
data class DialogueContinueMessage(val hash: Int, val index: Int) : Message {
    companion object : MessageCompanion<DialogueContinueMessage>()
}