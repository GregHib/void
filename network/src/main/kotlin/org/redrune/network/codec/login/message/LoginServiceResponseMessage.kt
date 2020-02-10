package org.redrune.network.codec.login.message

import org.redrune.network.model.message.Message

/**
 * This class contains the data that will be sent back to the client when it requests the login protocol
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
data class LoginServiceResponseMessage(val responseCode: Int) : Message