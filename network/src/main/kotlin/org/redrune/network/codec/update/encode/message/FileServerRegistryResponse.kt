package org.redrune.network.codec.update.encode.message

import org.redrune.network.message.Message
import org.redrune.tools.constants.LoginResponseCodes

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class FileServerRegistryResponse(val opcode: Int) : Message {
    constructor(response: LoginResponseCodes): this(response.opcode)
}