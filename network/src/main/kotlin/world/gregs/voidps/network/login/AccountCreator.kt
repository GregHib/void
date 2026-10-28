package world.gregs.voidps.network.login

/**
 * Creates accounts requested from the client's login screen
 */
interface AccountCreator {
    /**
     * Checks whether [email] can be used to create a new account
     * @return a [world.gregs.voidps.network.login.registration.RegistrationResponse] code
     */
    fun available(email: String): Int

    /**
     * Creates and persists a new account
     * @return a [world.gregs.voidps.network.login.registration.RegistrationResponse] code
     */
    suspend fun create(registration: Registration): Int
}

data class Registration(
    val email: String,
    val passwordHash: String,
    val hostname: String,
    val language: Int = 0,
    val affiliate: Int = 0,
    val optIn: Boolean = false,
)
