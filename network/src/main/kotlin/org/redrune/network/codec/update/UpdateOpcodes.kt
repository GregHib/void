package org.redrune.network.codec.update

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-02-02
 */
object UpdateOpcodes {
    const val FILE_REQUEST = 0
    const val PRIORITY_FILE_REQUEST = 1
    const val STATUS_LOGGED_IN = 2
    const val STATUS_LOGGED_OUT = 3
    const val ENCRYPTION = 4
    const val CONNECTED = 6
    const val DISCONNECT = 7
}