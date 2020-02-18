package org.redrune.network.codec.login.message

import org.redrune.network.codec.login.LoginResponseCode
import org.redrune.network.model.message.Message

/**
 * This class contains the data
 * @author Tyluur <contact@kiaira.tech>
 * @since February 10, 2020
 */
data class LoginResponseMessage(val responseCode: Int) : Message {
    constructor(responseCode: LoginResponseCode) : this(responseCode.opcode)
}