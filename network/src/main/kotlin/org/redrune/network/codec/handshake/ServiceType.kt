package org.redrune.network.codec.handshake

/**
 * This class contains the opcodes that identify any possible service
 *
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020
 */
class ServiceType(val opcode: Int) {

    companion object {

        const val SERVICE_UPDATE = 15

        const val SERVICE_LOGIN = 14
    }
}
