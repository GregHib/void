package world.gregs.voidps.web.account

import org.mindrot.jbcrypt.BCrypt
import world.gregs.voidps.engine.client.AccountUpdate
import world.gregs.voidps.engine.client.PlayerAccountLoader
import world.gregs.voidps.engine.client.PlayerAccountUpdater
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.DisplayNames
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.network.login.AccountNames
import world.gregs.voidps.network.login.registration.RegistrationLimiter
import world.gregs.voidps.network.login.registration.RegistrationResponse
import world.gregs.voidps.network.login.registration.RegistrationValidator
import world.gregs.voidps.web.api.ApiException
import world.gregs.voidps.web.api.model.AccountResponse

/**
 * Creates and changes accounts on behalf of a trusted backend such as a website, applying changes to the
 * running server so they take effect without an account reload.
 */
class AccountService(
    private val loader: PlayerAccountLoader,
    private val updater: PlayerAccountUpdater,
    private val definitions: AccountDefinitions,
    private val limiter: RegistrationLimiter,
) {

    /**
     * Looks up an account by its account name, never its display name, so a request can only ever reach one account
     */
    fun account(name: String): AccountResponse? {
        val definition = definitions.getByAccount(accountName(name)) ?: return null
        return AccountResponse(definition.accountName, definition.displayName, definition.previousName, Players.findByAccount(definition.accountName) != null)
    }

    /**
     * Whether [name] is free to use as a new account name or display name
     */
    fun available(name: String): Boolean {
        val accountName = accountName(name)
        validateName(accountName)
        return !definitions.taken(accountName)
    }

    /**
     * Creates an account for a username or email [name]
     * @param displayName required when [name] is an email address, otherwise must be absent or equal to [name]
     * @param ip address of the end user, used for rate limiting
     */
    suspend fun create(name: String, password: String, displayName: String?, ip: String): AccountResponse {
        if (!Settings["web.api.registration", true]) {
            throw ApiException.Forbidden("Account creation is disabled.")
        }
        val email = AccountNames.isEmail(name)
        val accountName = accountName(name)
        val display = displayName?.let { normalise(it) }
        validateName(accountName)
        if (email && (display == null || !DisplayNames.valid(display)) || !email && display != null && display != accountName) {
            throw ApiException.Validation("displayName", "Display name must be 1-12 letters, numbers or spaces and is required for email accounts.")
        }
        validatePassword(password, accountName)
        // Only well-formed requests count towards the limit so typos don't lock a player out
        if (!limiter.allow(ip)) {
            throw ApiException.RateLimited(retryAfterSeconds = ((limiter.remaining(ip) + 999) / 1000).toInt().coerceAtLeast(1))
        }
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        when (loader.create(accountName, hash, if (email) display else null, ip)) {
            RegistrationResponse.SUCCESS -> return account(accountName) ?: throw unavailable()
            RegistrationResponse.EMAIL_IN_USE -> throw nameTaken()
            RegistrationResponse.REFUSED -> throw ApiException.Forbidden("Account creation is disabled.")
            else -> throw unavailable()
        }
    }

    /**
     * Sets the password of account [name]; logs the player out if they're online
     */
    suspend fun password(name: String, password: String) {
        val accountName = existing(name)
        validatePassword(password, accountName)
        val hash = BCrypt.hashpw(password, BCrypt.gensalt())
        updater.password(accountName, hash).check(name)
    }

    /**
     * Changes the display name of account [name]
     */
    suspend fun rename(name: String, displayName: String): AccountResponse {
        val accountName = existing(name)
        updater.rename(accountName, normalise(displayName)).check(name)
        return account(accountName) ?: throw unavailable()
    }

    /**
     * The stored account name of account [name], which may differ in case or spacing
     */
    private fun existing(name: String): String = definitions.getByAccount(accountName(name))?.accountName ?: throw ApiException.NotFound("account", name)

    private fun validateName(accountName: String) {
        val valid = if (AccountNames.isEmail(accountName)) RegistrationValidator.email(accountName) == RegistrationResponse.SUCCESS else DisplayNames.valid(accountName)
        if (!valid) {
            throw ApiException.Validation("name", "Name must be 1-12 letters, numbers or spaces, or a valid email address.")
        }
    }

    private fun validatePassword(password: String, accountName: String) {
        if (RegistrationValidator.password(password, accountName) != RegistrationResponse.SUCCESS) {
            throw ApiException.Validation("password", "Password must be 5-20 letters or numbers and not easily guessed.")
        }
    }

    private fun AccountUpdate.check(name: String) {
        when (this) {
            AccountUpdate.SUCCESS -> return
            AccountUpdate.NOT_FOUND -> throw ApiException.NotFound("account", name)
            AccountUpdate.INVALID -> throw ApiException.Validation("displayName", "Display name must be 1-12 letters, numbers or spaces.")
            AccountUpdate.TAKEN -> throw nameTaken()
            AccountUpdate.BUSY -> throw ApiException.Conflict("account_busy", "Account is being saved, try again shortly.")
            AccountUpdate.UNAVAILABLE -> throw unavailable()
            AccountUpdate.PROTECTED -> throw ApiException.Forbidden("Staff accounts can only be changed in-game.")
        }
    }

    private fun nameTaken() = ApiException.Conflict("name_taken", "That name is already in use.")

    private fun unavailable() = ApiException.Unavailable("The game server could not complete the request.")

    private companion object {
        private val spaces = Regex(" +")

        /**
         * Underscores and dashes are shown as spaces in display names, matching the client's name entry
         */
        fun normalise(name: String): String = name.replace('_', ' ').replace('-', ' ').replace(spaces, " ").trim()

        /**
         * Emails are compared in lowercase, usernames get the same spacing as display names
         */
        fun accountName(name: String): String = if (AccountNames.isEmail(name)) name.lowercase() else normalise(name)
    }
}
