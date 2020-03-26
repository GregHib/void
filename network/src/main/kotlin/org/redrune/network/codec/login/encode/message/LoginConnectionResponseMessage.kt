package org.redrune.network.codec.login.encode.message

import org.redrune.core.network.model.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class LoginConnectionResponseMessage(val opcode: Int) : Message