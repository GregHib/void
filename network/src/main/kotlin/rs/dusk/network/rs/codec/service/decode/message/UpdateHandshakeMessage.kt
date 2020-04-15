package rs.dusk.network.rs.codec.service.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class UpdateHandshakeMessage(val major: Int) : Message