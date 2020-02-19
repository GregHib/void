package org.redrune.network.codec.login.encode.message

import org.redrune.network.message.Message

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class LoginServiceResponseMessage(val opcode: Int) : Message