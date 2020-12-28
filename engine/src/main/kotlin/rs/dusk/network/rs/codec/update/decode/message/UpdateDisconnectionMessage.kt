package rs.dusk.network.rs.codec.update.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class UpdateDisconnectionMessage(val value: Int) : Message