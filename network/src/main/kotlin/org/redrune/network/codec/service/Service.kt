package org.redrune.network.codec.service

import org.redrune.network.codec.service.ServiceOpcodes.Companion.FILE_SERVICE_OPCODE
import org.redrune.network.codec.service.ServiceOpcodes.Companion.LOGIN_SERVICE_OPCODE

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020
 */
enum class Service(val opcode: Int) {

    /**
     * The file service
     */
    FILE_SERVICE(FILE_SERVICE_OPCODE),

    LOGIN_SERVICE(LOGIN_SERVICE_OPCODE)

}

class ServiceOpcodes {

    companion object {
        /**
         * The js5-request opcode.
         */
        const val FILE_SERVICE_OPCODE = 15
        /**
         * The login request opcode.
         */
        const val LOGIN_SERVICE_OPCODE = 14
    }
}