package rs.dusk.network.rs.codec.update.encode.message

import rs.dusk.core.network.model.message.Message
import rs.dusk.network.rs.codec.login.decode.LoginResponseCode

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
data class UpdateRegistryResponse(val opcode: Int) : Message {

    constructor(response: LoginResponseCode) : this(response.opcode)

}