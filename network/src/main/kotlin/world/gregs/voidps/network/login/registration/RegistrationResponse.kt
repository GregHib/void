package world.gregs.voidps.network.login.registration

/**
 * Single byte replies to the client's account creation requests ([world.gregs.voidps.network.Request.SIGN_UP]
 * email availability checks and [world.gregs.voidps.network.Request.CREATE_ACCOUNT] sign up forms).
 * Values are what the 634 client scripts translate into on-screen messages and differ from login [world.gregs.voidps.network.Response]s.
 */
object RegistrationResponse {
    const val SUCCESS = 2
    const val BUSY = 7
    const val REFUSED = 9
    const val EMAIL_IN_USE = 20
    const val INVALID_EMAIL = 21
    const val PASSWORD_LENGTH = 30
    const val PASSWORD_CHARACTERS = 31
    const val PASSWORD_GUESSABLE = 32
    const val CLIENT_OUTDATED = 37
    const val UNAVAILABLE = 38
}
