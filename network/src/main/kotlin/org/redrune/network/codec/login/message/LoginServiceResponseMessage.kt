package org.redrune.network.codec.login.message

import org.redrune.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
data class LoginServiceResponseMessage(val responseCode: Int) : Message