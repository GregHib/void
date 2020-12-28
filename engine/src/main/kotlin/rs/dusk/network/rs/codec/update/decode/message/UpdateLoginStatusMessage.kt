package rs.dusk.network.rs.codec.update.decode.message

import rs.dusk.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class UpdateLoginStatusMessage(val online: Boolean, val value: Int) : Message