package rs.dusk.network.rs.codec.game.decode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.game.MessageCompanion

/**
 * When a interface button is clicked directly or using a right click option choice
 * @param hash The interface and component id combined
 * @param paramOne Optional starting slot index
 * @param paramTwo Optioning finishing slot index
 * @param option The menu option index
 */
data class InterfaceOptionMessage(val hash: Int, val paramOne: Int, val paramTwo: Int, val option: Int) : Message {
    companion object : MessageCompanion<InterfaceOptionMessage>()
}