package org.redrune.network.codec.update.encode.message

import org.redrune.core.network.message.Message
import org.redrune.tools.constants.LoginResponseCodes

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class UpdateRegistryResponse(val opcode: Int) : Message {

    constructor(response: LoginResponseCodes) : this(response.opcode)

}