package rs.dusk.network.rs.codec.update.encode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.utility.constants.network.LoginResponseCodes

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class UpdateRegistryResponse(val opcode: Int) : Message {

    constructor(response: LoginResponseCodes) : this(response.opcode)

}