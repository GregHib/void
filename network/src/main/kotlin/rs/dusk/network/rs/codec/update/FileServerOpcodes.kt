package rs.dusk.network.rs.codec.update

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since February 18, 2020
 */
object FileServerOpcodes {
    const val FILE_REQUEST = 0
    const val PRIORITY_FILE_REQUEST = 1
    const val STATUS_LOGGED_IN = 2
    const val STATUS_LOGGED_OUT = 3
    const val ENCRYPTION = 4
    const val CONNECTED = 6
    const val DISCONNECTED = 7
}